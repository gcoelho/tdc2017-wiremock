package com.hpe.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import java.util.regex.Matcher;

import static com.hpe.wiremock.ResourceIdMapper.UUID_PATTERN;
import static com.hpe.wiremock.ResourceIdMapper.fromOriginal;

public class ResourceIdResponseTransformer extends ResponseDefinitionTransformer {

    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
        String contents = this.buildResponseBodyFromFile(responseDefinition, files);

        return ResponseDefinitionBuilder.like(responseDefinition)
                .withBody(contents)
                .build();
    }

    @Override
    public String getName() {
        return "resource-id-response-transformer";
    }

    private String buildResponseBodyFromFile(ResponseDefinition responseDefinition, FileSource files) {
        TextFile bodyFile = files.getTextFileNamed(responseDefinition.getBodyFileName());
        Matcher matcher = UUID_PATTERN.matcher(bodyFile.readContentsAsString());
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String resourceId = matcher.group(0);
            String newResourceId = fromOriginal(resourceId);

            System.out.println("Original: [" + resourceId + "] Modified: [" + newResourceId + "]");

            matcher.appendReplacement(result, newResourceId);
        }
        matcher.appendTail(result);

        return result.toString();
    }

}
