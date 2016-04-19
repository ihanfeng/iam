package it.infn.mw.iam.saml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import it.infn.mw.iam.persistence.model.Authority;
import it.infn.mw.iam.persistence.model.IamAccount;
import it.infn.mw.iam.persistence.repository.IamAccountRespository;
import it.infn.mw.iam.saml.util.SAMLUserIdentifierAccessor;

public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

  @Autowired
  SAMLUserIdentifierAccessor userIdAccessor;

  @Autowired
  IamAccountRespository repo;

  List<GrantedAuthority> convertAuthorities(IamAccount a) {

    List<GrantedAuthority> authorities = new ArrayList<>();
    for (Authority auth : a.getAuthorities()) {
      authorities.add(new SimpleGrantedAuthority(auth.getAuthority()));
    }
    return authorities;
  }

  @Override
  public Object loadUserBySAML(SAMLCredential credential)
    throws UsernameNotFoundException {

    String issuerId = credential.getRemoteEntityID();
    String userSamlId = userIdAccessor.getUserIdentifier(credential);

    if (userSamlId == null) {
      throw new UsernameNotFoundException("No NameID found in SAML assertion");
    }

    Optional<IamAccount> account = repo.findBySamlAccount(issuerId, userSamlId);

    if (account.isPresent()) {

      IamAccount a = account.get();

      User u = new User(a.getUsername(), a.getPassword(),
        convertAuthorities(a));

      return u;

    }

    return null;

  }

}