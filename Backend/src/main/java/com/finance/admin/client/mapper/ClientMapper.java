package com.finance.admin.client.mapper;

import com.finance.admin.client.dto.ClientResponse;
import com.finance.admin.client.dto.CreateClientRequest;
import com.finance.admin.client.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(source = "client", target = "fullName", qualifiedByName = "getFullName")
    ClientResponse toResponse(Client client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "membershipNumber", ignore = true)
    @Mapping(target = "tfnEncrypted", ignore = true)
    @Mapping(target = "bankAccountNumberEncrypted", ignore = true)
    @Mapping(target = "blockchainIdentityHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Client toEntity(CreateClientRequest request);

    @Named("getFullName")
    default String getFullName(Client client) {
        return client.getFullName();
    }
} 