package com.ago.camunda.config;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.IdentityOperationResult;
import org.camunda.bpm.engine.impl.identity.WritableIdentityProvider;
import org.springframework.stereotype.Component;

@Component
public class CustomWritableProvider implements WritableIdentityProvider {


    @Override
    public User createNewUser(String s) {
        return null;
    }

    @Override
    public IdentityOperationResult saveUser(User user) {
        return null;
    }

    @Override
    public IdentityOperationResult deleteUser(String s) {
        return null;
    }

    @Override
    public IdentityOperationResult unlockUser(String s) {
        return null;
    }

    @Override
    public Group createNewGroup(String s) {
        return null;
    }

    @Override
    public IdentityOperationResult saveGroup(Group group) {
        return null;
    }

    @Override
    public IdentityOperationResult deleteGroup(String s) {
        return null;
    }

    @Override
    public Tenant createNewTenant(String s) {
        return null;
    }

    @Override
    public IdentityOperationResult saveTenant(Tenant tenant) {
        return null;
    }

    @Override
    public IdentityOperationResult deleteTenant(String s) {
        return null;
    }

    @Override
    public IdentityOperationResult createMembership(String s, String s1) {
        return null;
    }

    @Override
    public IdentityOperationResult deleteMembership(String s, String s1) {
        return null;
    }

    @Override
    public IdentityOperationResult createTenantUserMembership(String s, String s1) {
        return null;
    }

    @Override
    public IdentityOperationResult createTenantGroupMembership(String s, String s1) {
        return null;
    }

    @Override
    public IdentityOperationResult deleteTenantUserMembership(String s, String s1) {
        return null;
    }

    @Override
    public IdentityOperationResult deleteTenantGroupMembership(String s, String s1) {
        return null;
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }
}
