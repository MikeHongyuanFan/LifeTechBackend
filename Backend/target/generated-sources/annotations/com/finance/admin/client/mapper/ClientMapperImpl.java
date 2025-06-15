package com.finance.admin.client.mapper;

import com.finance.admin.client.dto.ClientResponse;
import com.finance.admin.client.dto.CreateClientRequest;
import com.finance.admin.client.model.Client;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-15T12:57:27+1000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
@Component
public class ClientMapperImpl implements ClientMapper {

    @Override
    public ClientResponse toResponse(Client client) {
        if ( client == null ) {
            return null;
        }

        ClientResponse.ClientResponseBuilder clientResponse = ClientResponse.builder();

        clientResponse.fullName( getFullName( client ) );
        clientResponse.id( client.getId() );
        clientResponse.membershipNumber( client.getMembershipNumber() );
        clientResponse.firstName( client.getFirstName() );
        clientResponse.middleName( client.getMiddleName() );
        clientResponse.lastName( client.getLastName() );
        clientResponse.dateOfBirth( client.getDateOfBirth() );
        clientResponse.emailPrimary( client.getEmailPrimary() );
        clientResponse.emailSecondary( client.getEmailSecondary() );
        clientResponse.phonePrimary( client.getPhonePrimary() );
        clientResponse.phoneSecondary( client.getPhoneSecondary() );
        clientResponse.addressStreet( client.getAddressStreet() );
        clientResponse.addressCity( client.getAddressCity() );
        clientResponse.addressState( client.getAddressState() );
        clientResponse.addressPostalCode( client.getAddressPostalCode() );
        clientResponse.addressCountry( client.getAddressCountry() );
        clientResponse.mailingAddressSame( client.getMailingAddressSame() );
        clientResponse.mailingStreet( client.getMailingStreet() );
        clientResponse.mailingCity( client.getMailingCity() );
        clientResponse.mailingState( client.getMailingState() );
        clientResponse.mailingPostalCode( client.getMailingPostalCode() );
        clientResponse.mailingCountry( client.getMailingCountry() );
        clientResponse.taxResidencyStatus( client.getTaxResidencyStatus() );
        clientResponse.bankBsb( client.getBankBsb() );
        clientResponse.bankAccountName( client.getBankAccountName() );
        clientResponse.investmentTarget( client.getInvestmentTarget() );
        clientResponse.riskProfile( client.getRiskProfile() );
        clientResponse.blockchainIdentityHash( client.getBlockchainIdentityHash() );
        clientResponse.status( client.getStatus() );
        clientResponse.createdAt( client.getCreatedAt() );
        clientResponse.updatedAt( client.getUpdatedAt() );
        if ( client.getCreatedBy() != null ) {
            clientResponse.createdBy( Long.parseLong( client.getCreatedBy() ) );
        }
        if ( client.getUpdatedBy() != null ) {
            clientResponse.updatedBy( Long.parseLong( client.getUpdatedBy() ) );
        }

        return clientResponse.build();
    }

    @Override
    public Client toEntity(CreateClientRequest request) {
        if ( request == null ) {
            return null;
        }

        Client.ClientBuilder client = Client.builder();

        client.firstName( request.getFirstName() );
        client.middleName( request.getMiddleName() );
        client.lastName( request.getLastName() );
        client.dateOfBirth( request.getDateOfBirth() );
        client.emailPrimary( request.getEmailPrimary() );
        client.emailSecondary( request.getEmailSecondary() );
        client.phonePrimary( request.getPhonePrimary() );
        client.phoneSecondary( request.getPhoneSecondary() );
        client.addressStreet( request.getAddressStreet() );
        client.addressCity( request.getAddressCity() );
        client.addressState( request.getAddressState() );
        client.addressPostalCode( request.getAddressPostalCode() );
        client.addressCountry( request.getAddressCountry() );
        client.mailingAddressSame( request.getMailingAddressSame() );
        client.mailingStreet( request.getMailingStreet() );
        client.mailingCity( request.getMailingCity() );
        client.mailingState( request.getMailingState() );
        client.mailingPostalCode( request.getMailingPostalCode() );
        client.mailingCountry( request.getMailingCountry() );
        client.taxResidencyStatus( request.getTaxResidencyStatus() );
        client.bankBsb( request.getBankBsb() );
        client.bankAccountName( request.getBankAccountName() );
        client.investmentTarget( request.getInvestmentTarget() );
        client.riskProfile( request.getRiskProfile() );

        return client.build();
    }
}
