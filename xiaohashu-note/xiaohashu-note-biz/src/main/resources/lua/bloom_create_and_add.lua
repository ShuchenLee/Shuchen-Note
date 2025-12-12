---
--- Created by LeeShuchen
--- DateTime: 2025/12/12 10:04
---
local redisKey = KEYS[1]
local noteId = ARGV[1]
local expireSecs = ARGV[2]
redis.call("BF.ADD",redisKey,noteId)
redis.call("EXPIRE",redisKey,expireSecs)
return 0
