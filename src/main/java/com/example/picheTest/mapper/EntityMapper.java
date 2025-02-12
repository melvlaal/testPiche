package com.example.picheTest.mapper;

import com.example.picheTest.model.request.AccountCreateRQ;
import com.example.picheTest.repository.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    @Mapping(target = "accountNumber", source = "accountNumber")
    @Mapping(target = "balance", source = "balance")
    Account toAccount(AccountCreateRQ entity);
}
