/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver;

import com.github.miltenplescott.pswdmngrserver.user.AuthenticationRequestFilter;
import com.github.miltenplescott.pswdmngrserver.user.UserResource;
import com.github.miltenplescott.pswdmngrserver.vault.VaultResource;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("pswd-mngr")
public class ApplicationConfig extends Application {

    public static final String UTF8_SUFFIX = "; charset=" + StandardCharsets.UTF_8.name();

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        resources.add(UserResource.class);
        resources.add(AuthenticationRequestFilter.class);
        resources.add(VaultResource.class);
        return resources;
    }

}
