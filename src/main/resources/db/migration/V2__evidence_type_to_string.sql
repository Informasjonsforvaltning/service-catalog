-- Evidence.type changed from List<String> to String.
-- Convert any stored array value to its first element (or remove the key if the array is empty).
UPDATE services
SET data = jsonb_set(
    data,
    '{evidence}',
    (
        SELECT jsonb_agg(
            CASE
                WHEN jsonb_typeof(ev -> 'type') = 'array' AND jsonb_array_length(ev -> 'type') > 0
                    THEN jsonb_set(ev, '{type}', ev -> 'type' -> 0)
                WHEN jsonb_typeof(ev -> 'type') = 'array'
                    THEN ev - 'type'
                ELSE ev
            END
            ORDER BY ord
        )
        FROM jsonb_array_elements(data -> 'evidence') WITH ORDINALITY AS e(ev, ord)
    )
)
WHERE jsonb_typeof(data -> 'evidence') = 'array'
  AND EXISTS (
      SELECT 1
      FROM jsonb_array_elements(data -> 'evidence') AS e(ev)
      WHERE jsonb_typeof(ev -> 'type') = 'array'
  );
