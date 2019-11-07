UPDATE intro
SET name=:(name), age=:(age), pronouns=:(pronouns), extra=:(extra)
WHERE discord_id=:(discord_id);