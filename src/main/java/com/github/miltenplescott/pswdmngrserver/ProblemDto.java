/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver;

import com.google.common.annotations.VisibleForTesting;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;

//@JsonbPropertyOrder({
//    "type",
//    "title",
//    "status",
//    "detail",
//    "instance",
//    "invalid-params"  // ?? https://github.com/eclipse-ee4j/yasson/issues/159
//})

@JsonbPropertyOrder({"type", "title", "status", "detail", "instance", "invalidParams"})
public final class ProblemDto {

    @JsonbTransient
    public static final String MEDIA_TYPE_PROBLEM_JSON = "application/problem+json";

    private URI type = null;
    private String title = null;
    private Integer status = null;
    private String detail = null;
    private URI instance = null;

    @JsonbProperty("invalid-params")
    private List<Extension> invalidParams = new ArrayList<>();

    public ProblemDto() {
    }

    public ProblemDto(String title) {
        this.title = title;
    }

    public ProblemDto(String title, String detail) {
        this.title = title;
        this.detail = detail;
    }

    @JsonbPropertyOrder({"name", "reason"})
    public final static class Extension {

        private String name;
        private String reason;

        public Extension() {
        }

        public Extension(String name, String reason) {
            this.name = name;
            this.reason = reason;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, reason);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Extension other = (Extension) obj;
            return Objects.equals(this.name, other.name)
                && Objects.equals(this.reason, other.reason);
        }

    }

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public URI getInstance() {
        return instance;
    }

    public void setInstance(URI instance) {
        this.instance = instance;
    }

    public List<Extension> getInvalidParams() {
        return invalidParams;  // don't return a copy
    }

    // needs to be public so that Jsonb#fromJson() can parse json strings correctly
    @VisibleForTesting
    public void setInvalidParams(List<Extension> invalidParams) {
        this.invalidParams = invalidParams;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title, status, detail, instance, invalidParams);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProblemDto other = (ProblemDto) obj;
        return Objects.equals(this.type, other.type)
            && Objects.equals(this.title, other.title)
            && Objects.equals(this.status, other.status)
            && Objects.equals(this.detail, other.detail)
            && Objects.equals(this.instance, other.instance)
            && Objects.equals(this.invalidParams, other.invalidParams);
    }

}
