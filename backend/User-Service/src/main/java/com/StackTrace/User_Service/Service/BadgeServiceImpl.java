package com.StackTrace.User_Service.Service;

import com.StackTrace.User_Service.Repository.BadgeRepository;
import com.StackTrace.User_Service.Repository.UserBadgeRepository;
import com.StackTrace.User_Service.Repository.UserRepository;
import com.StackTrace.User_Service.dto.BadgeResponse;
import com.StackTrace.User_Service.exception.BadgeAlreadyEarnedException;
import com.StackTrace.User_Service.exception.BadgeNotFoundException;
import com.StackTrace.User_Service.exception.UserNotFoundException;
import com.StackTrace.User_Service.model.Badge;
import com.StackTrace.User_Service.model.User;
import com.StackTrace.User_Service.model.UserBadge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BadgeServiceImpl
        implements BadgeService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    @Override
    public void awardBadge(
            Long userId,
            Long badgeId
    ) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"
                        ));

        Badge badge = badgeRepository
                .findById(badgeId)
                .orElseThrow(() ->
                        new BadgeNotFoundException(
                                "Badge not found"
                        ));

        if(userBadgeRepository
                .existsByUser_IdAndBadge_Id(
                        userId,
                        badgeId
                )) {

            throw new BadgeAlreadyEarnedException(
                    "Badge already earned"
            );
        }

        UserBadge userBadge =
                UserBadge.builder()
                        .user(user)
                        .badge(badge)
                        .build();

        userBadgeRepository.save(
                userBadge
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeResponse> getUserBadges(
            Long userId
    ) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"
                        ));

        return userBadgeRepository
                .findByUser_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private BadgeResponse mapToResponse(
            UserBadge userBadge
    ) {

        Badge badge =
                userBadge.getBadge();

        return BadgeResponse.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(
                        badge.getDescription()
                )
                .icon(
                        badge.getIcon()
                )
                .earnedAt(
                        userBadge.getEarnedAt()
                )
                .build();
    }
}
