package com.quanxiaoha.kv.biz.domain.respsitory;

import com.quanxiaoha.kv.biz.domain.dataobject.NoteContentDO;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface NoteContentRepository extends CassandraRepository<NoteContentDO, UUID> {
}
