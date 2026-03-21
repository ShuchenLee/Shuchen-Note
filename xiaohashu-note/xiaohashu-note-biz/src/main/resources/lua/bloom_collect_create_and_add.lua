---
--- Created by LeeShuchen
--- DateTime: 2025/12/13 16:57
---
local redisKey = KEYS[1]
local noteId = ARGV[1]
local expireSecs = ARGV[2]
redis.call("BF.ADD",redisKey,noteId)
redis.call("EXPIRE",redisKey,expireSecs)
return 0