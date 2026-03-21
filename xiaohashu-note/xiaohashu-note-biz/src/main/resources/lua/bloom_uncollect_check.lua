---
--- Created by LeeShuchen
--- DateTime: 2025/12/13 20:09
---
local redisKey = KEYS[1]
local noteId = ARGV[1]
local exist = redis.call("EXISTS",redisKey)
if exist == 0 then
    return -1
end
return redis.call("BF.EXISTS",redisKey,noteId)