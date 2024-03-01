package com.spring.batch.common.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Setter
@Getter
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class TargetDept {

    @Id
    Integer deptNo;
    String dName;
    String loc;
}
