package com.StackTrace.User_Service.Service;

import com.StackTrace.User_Service.Repository.FollowRepository;
import com.StackTrace.User_Service.Repository.SkillRepository;
import com.StackTrace.User_Service.Repository.UserRepository;
import com.StackTrace.User_Service.Security.CurrentUser;
import com.StackTrace.User_Service.dto.*;
import com.StackTrace.User_Service.exception.*;
import com.StackTrace.User_Service.model.Follow;
import com.StackTrace.User_Service.model.Skill;
import com.StackTrace.User_Service.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    /** Upper bound for any page size requested by a client. */
    public static final int MAX_PAGE_SIZE = 50;

    private final BCryptPasswordEncoder bp;
    private final UserRepository up;
    private final JwtService jwtService;
    private final FollowRepository fp;
    private final SkillRepository sp;
    private final RefreshTokenService refreshTokens;
    private final BadgeService badgeService;

    /** Used to keep login timing similar whether or not the email exists. */
    private String dummyHash;

    // ---------------------------------------------------------------- auth

    @Transactional
    public RegisterResponse register(RegisterRequest rq) {
        String email = rq.getEmail().trim().toLowerCase(Locale.ROOT);
        String username = rq.getUsername().trim();
        if (up.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email Already Registered");
        }
        if (up.existsByUsernameIgnoreCase(username)) {
            throw new UsernameAlreadyExistsException("Username already taken");
        }
        User u = User.builder()
                .name(rq.getName().trim())
                .email(email)
                .username(username)
                .password(bp.encode(rq.getPassword()))
                .build();
        User ret = up.save(u);
        badgeService.awardThresholdBadges(ret);
        RegisterResponse rp = new RegisterResponse();
        rp.setId(ret.getId());
        rp.setName(ret.getName());
        rp.setEmail(ret.getEmail());
        rp.setUsername(ret.getUsername());
        return rp;
    }

    @Transactional
    public LoginResponse login(LoginRequest l) {
        String email = l.getEmail().trim().toLowerCase(Locale.ROOT);
        User user = up.findByEmail(email).orElse(null);
        if (user == null) {
            bp.matches(l.getPassword(), dummyHash());
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (!bp.matches(l.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return tokensFor(user, refreshTokens.issue(user.getId()));
    }

    /** noRollbackFor: on token reuse the "revoke all sessions" write must survive the 401. */
    @Transactional(noRollbackFor = InvalidTokenException.class)
    public LoginResponse refresh(String refreshToken) {
        RefreshTokenService.Rotation rotation = refreshTokens.rotate(refreshToken);
        User user = up.findById(rotation.userId())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        return tokensFor(user, rotation.newToken());
    }

    public void logout(String refreshToken) {
        refreshTokens.revoke(refreshToken);
    }

    private LoginResponse tokensFor(User user, String refreshToken) {
        return LoginResponse.builder()
                .token(jwtService.generateToken(user.getId(), user.getUsername()))
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationSeconds())
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    private String dummyHash() {
        if (dummyHash == null) {
            dummyHash = bp.encode("timing-equalizer-not-a-password");
        }
        return dummyHash;
    }

    // ---------------------------------------------------------------- profile

    @Transactional(readOnly = true)
    public UserProfileResponse getUser(Long id) {
        return toProfile(findUser(id));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserByUsername(String username) {
        User u = up.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User Does Not Exist"));
        return toProfile(u);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse me() {
        return toProfile(findUser(CurrentUser.id()));
    }

    @Transactional
    public UserProfileResponse update(UpdateProfileRequest ur) {
        User u = findUser(CurrentUser.id());
        if (ur.getName() != null && !ur.getName().isBlank()) {
            u.setName(ur.getName().trim());
        }
        if (ur.getBio() != null) {
            u.setBio(ur.getBio());
        }
        if (ur.getAvatarUrl() != null) {
            u.setAvatarUrl(ur.getAvatarUrl().isBlank() ? null : ur.getAvatarUrl());
        }
        if (ur.getLocation() != null) {
            u.setLocation(ur.getLocation());
        }
        return toProfile(up.save(u));
    }

    private UserProfileResponse toProfile(User u) {
        return UserProfileResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .username(u.getUsername())
                .bio(u.getBio())
                .avatarUrl(u.getAvatarUrl())
                .points(u.getPoints())
                .location(u.getLocation())
                .joinedAt(u.getCreatedAt())
                .rank(up.countWithMorePoints(u.getPoints()) + 1)
                .followersCount(fp.countByFollowingId(u.getId()))
                .followingCount(fp.countByFollowerId(u.getId()))
                .skills(skillsOf(u.getId()))
                .badges(badgeService.getUserBadges(u.getId()))
                .build();
    }

    private User findUser(Long id) {
        return up.findById(id).orElseThrow(() -> new UserNotFoundException("User Does Not Exist"));
    }

    // ---------------------------------------------------------------- points (internal)

    @Transactional
    public UserProfileResponse addPoints(Long userId, int delta) {
        findUser(userId);
        up.addPoints(userId, delta);
        User u = findUser(userId);
        badgeService.awardThresholdBadges(u);
        return toProfile(u);
    }

    // ---------------------------------------------------------------- follow

    @Transactional
    public ApiResponse followUser(Long targetId) {
        Long me = CurrentUser.id();
        User targetUser = findUser(targetId);
        if (me.equals(targetUser.getId())) {
            throw new SelfFollowException("You cannot follow yourself");
        }
        if (fp.existsByFollowerIdAndFollowingId(me, targetId)) {
            throw new AlreadyFollowingException("Already following this user");
        }
        fp.save(Follow.builder().followerId(me).followingId(targetId).build());
        return ApiResponse.builder().success(true).message("User followed successfully").build();
    }

    @Transactional
    public ApiResponse unfollowUser(Long targetId) {
        Long me = CurrentUser.id();
        findUser(targetId);
        Follow follow = fp.findByFollowerIdAndFollowingId(me, targetId)
                .orElseThrow(() -> new BusinessException("Follow relationship not found"));
        fp.delete(follow);
        return ApiResponse.builder().success(true).message("User unfollowed successfully").build();
    }

    @Transactional(readOnly = true)
    public FollowStatusResponse followStatus(Long targetId) {
        return new FollowStatusResponse(fp.existsByFollowerIdAndFollowingId(CurrentUser.id(), targetId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> getFollowers(Long id, int page, int size) {
        findUser(id);
        return PagedResponse.of(fp.findFollowers(id, pageable(page, size)));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> getFollowing(Long id, int page, int size) {
        findUser(id);
        return PagedResponse.of(fp.findFollowing(id, pageable(page, size)));
    }

    @Transactional(readOnly = true)
    public List<Long> getFollowingIds(Long id) {
        return fp.findFollowingIds(id);
    }

    // ---------------------------------------------------------------- leaderboard

    @Transactional(readOnly = true)
    public PagedResponse<LeaderboardResponse> getLeaderboard(int page, int size) {
        PageRequest pageable = pageable(page, size);
        Page<User> users = up.findAllByOrderByPointsDescIdAsc(pageable);
        int rank = (int) pageable.getOffset() + 1;
        List<LeaderboardResponse> leaderboard = new ArrayList<>();
        for (User user : users.getContent()) {
            leaderboard.add(LeaderboardResponse.builder()
                    .rank(rank++)
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .avatarUrl(user.getAvatarUrl())
                    .points(user.getPoints())
                    .build());
        }
        return PagedResponse.<LeaderboardResponse>builder()
                .content(leaderboard)
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .last(users.isLast())
                .build();
    }

    // ---------------------------------------------------------------- skills

    @Transactional
    public SkillResponse addSkill(SkillRequest request) {
        Long me = CurrentUser.id();
        String name = request.getSkillName().trim();
        if (sp.existsByUserIdAndSkillName(me, name)) {
            throw new SkillAlreadyExistsException("Skill already exists");
        }
        Skill savedSkill = sp.save(Skill.builder().userId(me).skillName(name).build());
        return SkillResponse.builder().id(savedSkill.getId()).skillName(savedSkill.getSkillName()).build();
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> getSkills(Long userId) {
        findUser(userId);
        return skillsOf(userId);
    }

    private List<SkillResponse> skillsOf(Long userId) {
        return sp.findByUserId(userId).stream()
                .map(skill -> SkillResponse.builder().id(skill.getId()).skillName(skill.getSkillName()).build())
                .toList();
    }

    @Transactional
    public ApiResponse deleteSkill(Long skillId) {
        Long me = CurrentUser.id();
        Skill skill = sp.findById(skillId).orElseThrow(() -> new SkillNotFoundException("Skill not found"));
        if (!skill.getUserId().equals(me)) {
            throw new AccessDeniedException("You cannot delete another user's skill");
        }
        sp.delete(skill);
        return ApiResponse.builder().success(true).message("Skill deleted successfully").build();
    }

    // ---------------------------------------------------------------- search & batch

    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> searchUsers(String keyword, int page, int size) {
        String raw = keyword == null ? "" : keyword.trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be empty");
        }
        if (raw.length() > 100) {
            raw = raw.substring(0, 100);
        }
        String escaped = raw.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        Page<User> users = up.search(escaped, raw, pageable(page, size));
        return PagedResponse.of(users.map(UserService::summary));
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> findByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return up.findByIdIn(ids).stream().map(UserService::summary).toList();
    }

    static UserSummaryResponse summary(User user) {
        return new UserSummaryResponse(user.getId(), user.getName(), user.getUsername(), user.getAvatarUrl());
    }

    static PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
    }
}
