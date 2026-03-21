---
--- Created by LeeShuchen
--- DateTime: 2025/12/12 09:21
---
local redisKey = KEYS[1]
local noteId = ARGV[1]
--if bloom filter exist
local exist = redis.call("EXISTS",redisKey)

if exist == 0 then
    return -1
end
--if have liked
local isLiked = redis.call("BF.EXISTS",redisKey,noteId)
if isLiked then
    return 1
end
--if not like
redis.call("BF.ADD",redisKey,noteId)
return 0

