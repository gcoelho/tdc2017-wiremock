package com.hpe.wiremock;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;

import java.util.regex.Matcher;

import static com.hpe.wiremock.ResourceIdMapper.UUID_PATTERN;
import static com.hpe.wiremock.ResourceIdMapper.toOriginal;

public class ResourceIdRequestMatcher extends RequestMatcherExtension {

    @Override
    public MatchResult match(Request request, Parameters parameters) {
        Matcher matcher = UUID_PATTERN.matcher(request.getUrl());

        try {
            if (matcher.find()) {
                String resourceId = matcher.group(0);
                String originalResourceId = toOriginal(resourceId);

                System.out.println("Original: [" + originalResourceId + "] Modified: [" + resourceId + "]");

                return MatchResult.of(parameters.getString("resourceId").equalsIgnoreCase(originalResourceId));
            }
        } catch (UUIDNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
        return MatchResult.of(false);
    }

    @Override
    public String getName() {
        return "resource-id-request-matcher";
    }
}
