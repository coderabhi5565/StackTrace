-- Load-test data for post-service (postdb): 20k published posts across 30 tags.
DO $$
DECLARE words text[] := ARRAY['spring','java','kubernetes','docker','react','postgres','redis','kafka','caching',
  'microservices','testing','security','performance','rust','golang','python','typescript','nextjs','graphql',
  'observability','latency','indexing','concurrency','threads','design','patterns','cloud','devops','linux','sql'];
BEGIN
  IF (SELECT count(*) FROM posts) < 1000 THEN
    INSERT INTO tags (id, name, created_at, updated_at)
    SELECT nextval('tag_sequence'), w, now(), now() FROM unnest(words) w
    ON CONFLICT (name) DO NOTHING;

    INSERT INTO posts (id, title, slug, content, status, author_id, published_at, excerpt, reading_time,
                       like_count, comment_count, created_at, updated_at)
    SELECT nextval('post_sequence'),
           initcap(words[1 + g % 30]) || ' and ' || words[1 + (g * 7) % 30] || ' in practice #' || g,
           'load-post-' || g,
           repeat('Deep dive into ' || words[1 + g % 30] || ' with ' || words[1 + (g * 3) % 30] || ' and '
                  || words[1 + (g * 11) % 30] || '. ', 120),
           'PUBLISHED', 1 + (g % 500), now() - (g % 720) * interval '1 hour',
           'An article about ' || words[1 + g % 30], 5,
           (random() * 200)::int, (random() * 40)::int, now(), now()
    FROM generate_series(1, 20000) g;

    INSERT INTO post_tag (post_id, tag_id)
    SELECT p.id, t.id FROM posts p JOIN tags t
      ON t.name = split_part(lower(p.title), ' ', 1) OR t.name = split_part(p.title, ' ', 3)
    ON CONFLICT DO NOTHING;
  END IF;
END $$;
ANALYZE;
