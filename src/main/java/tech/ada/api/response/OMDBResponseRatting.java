package tech.ada.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OMDBResponseRatting {

    @JsonProperty("Source")
    private String source;
    @JsonProperty("Value")
    private String value;

}
