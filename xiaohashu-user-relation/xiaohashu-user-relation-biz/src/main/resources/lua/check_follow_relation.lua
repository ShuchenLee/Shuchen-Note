--get all info
local redisKey = KEYS[1]
local unfollowUserId = ARGV[1]
-- 使用 EXISTS 命令检查 ZSET 是否存在
local exists = redis.call('EXISTS', redisKey)
if exists == 0 then
    return -1
end
--if follow relation exist
local score = redis.call("ZSCORE",redisKey,unfollowUserId)
if score == false or score == nil then
    return -4
end

redis.call("ZREM",redisKey,unfollowUserId)
return 1