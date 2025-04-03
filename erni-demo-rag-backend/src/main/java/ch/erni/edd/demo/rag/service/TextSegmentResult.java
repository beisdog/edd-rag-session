package ch.erni.edd.demo.rag.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TextSegmentResult {
    public String text;
    public Map<String, Object> metadata;
    public String namespace;
}
