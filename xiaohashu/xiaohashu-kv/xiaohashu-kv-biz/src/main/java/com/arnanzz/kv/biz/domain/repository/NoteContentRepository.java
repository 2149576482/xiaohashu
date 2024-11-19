package com.arnanzz.kv.biz.domain.repository;

import com.arnanzz.kv.biz.domain.entity.NoteContentDO;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 泛型 实体类，主键类型
 **/
public interface NoteContentRepository extends CassandraRepository<NoteContentDO, UUID> {

}
