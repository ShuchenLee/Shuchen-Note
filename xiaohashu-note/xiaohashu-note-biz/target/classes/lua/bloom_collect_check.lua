---
--- Created by LeeShuchen
--- DateTime: 2025/12/13 16:17
---
local redisKey = KEYS[1]
local noteId = ARGV[1]
--if bloom exist
local exist = redis.call("EXISTS",redisKey)
if exist == 0 then
    return -1
end
local isCollected = redis.call("BF.EXISTS",redisKey,noteId)
if isCollected==1 then
    return 1
end
redisKey.call("BF.ADD",redisKey,noteId)
return 0
