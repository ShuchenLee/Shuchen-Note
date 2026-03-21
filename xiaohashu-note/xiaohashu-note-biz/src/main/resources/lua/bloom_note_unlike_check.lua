---
--- Created by LeeShuchen
--- DateTime: 2025/12/12 19:53
---
local redisKey = KEYS[1]
local noteId = ARGV[1]
--if bloom filter exist
local exist = redis.call("EXISTS",redisKey)
if exist == 0 then
    return -1
end
--if have liked
return redis.call("BF.EXISTS",redisKey,noteId)

