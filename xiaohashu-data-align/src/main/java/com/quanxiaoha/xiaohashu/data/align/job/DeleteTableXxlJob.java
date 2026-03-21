package com.quanxiaoha.xiaohashu.data.align.job;

import com.quanxiaoha.xiaohashu.data.align.constants.TableConstants;
import com.quanxiaoha.xiaohashu.data.align.domain.mapper.DeleteTempTableMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DeleteTableXxlJob {
    @Value("${table.shards}")
    private int tableShards;
    @Resource
    private CreateTableXxlJob createTableXxlJob;
    @Resource
    private DeleteTempTableMapper deleteTempTableMapper;

    @XxlJob("DeleteTableJobHandler")
    private void createJobHandler() throws Exception{
        XxlJobHelper.log("=========start to delete temp data increment tables in last month");
        LocalDate endDate = LocalDate.now().plusDays(1);
        LocalDate startDate = endDate.minusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        while(startDate.isBefore(endDate)){
            String dataString = formatter.format(startDate);
            for (int i = 0;i < tableShards;i++){
                String tableSuffix = TableConstants.buildTableNameSuffix(dataString,i);
                deleteTempTableMapper.deleteDataAlignFansCountTempTable(tableSuffix);
                deleteTempTableMapper.deleteDataAlignFollowingCountTempTable(tableSuffix);
                deleteTempTableMapper.deleteDataAlignNoteCollectCountTempTable(tableSuffix);
                deleteTempTableMapper.deleteDataAlignNoteLikeCountTempTable(tableSuffix);
                deleteTempTableMapper.deleteDataAlignUserCollectCountTempTable(tableSuffix);
                deleteTempTableMapper.deleteDataAlignUserLikeCountTempTable(tableSuffix);
                deleteTempTableMapper.deleteDataAlignNotePublishCountTempTable(tableSuffix);
            }
            startDate = startDate.plusDays(1);
        }
        XxlJobHelper.log("=========delete temp data increment tables in last month successfully");
    }
}
