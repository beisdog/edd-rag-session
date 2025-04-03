package ch.erni.edd.demo.rag.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchInput {
    public String question;
    public int maxResults;
}
