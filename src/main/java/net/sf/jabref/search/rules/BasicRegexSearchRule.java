/*  Copyright (C) 2003-2011 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref.search.rules;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.export.layout.format.RemoveLatexCommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Search rule for simple search.
 */
public class BasicRegexSearchRule extends BasicSearchRule {

    private static final RemoveLatexCommands removeBrackets = new RemoveLatexCommands();

    public BasicRegexSearchRule(boolean caseSensitive) {
        super(caseSensitive);
    }

    @Override
    public boolean validateSearchStrings(String query) {
        String searchString = query;
        if (!caseSensitive) {
            searchString = searchString.toLowerCase();
        }
        List<String> words = parseQuery(searchString);
        try {
            for (String word : words) {
                Pattern.compile(word, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
            }
        } catch (PatternSyntaxException ex) {
            return false;
        }
        return true;
    }

    @Override
    public int applyRule(String query, BibtexEntry bibtexEntry) {

        String searchString = query;
        if (!caseSensitive) {
            searchString = searchString.toLowerCase();
        }

        List<String> words = parseQuery(searchString);

        List<Pattern> patterns = new ArrayList<Pattern>();
        try {
            for (String word : words) {
                patterns.add(Pattern.compile(word, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE));
            }
        } catch (PatternSyntaxException ex) {
            return 0;
        }

        //print(words);
        // We need match for all words:
        boolean[] matchFound = new boolean[words.size()];

        for (String field : bibtexEntry.getAllFields()) {
            Object fieldContentAsObject = bibtexEntry.getField(field);
            if (fieldContentAsObject != null) {
                String fieldContent = BasicRegexSearchRule.removeBrackets.format(fieldContentAsObject.toString());
                if (!caseSensitive) {
                    fieldContent = fieldContent.toLowerCase();
                }

                int index = 0;
                // Check if we have a match for each of the query words, ignoring
                // those words for which we already have a match:
                for (Pattern pattern : patterns) {
                    String fieldContentNoBrackets = BasicRegexSearchRule.removeBrackets.format(fieldContent);
                    Matcher m = pattern.matcher(fieldContentNoBrackets);
                    matchFound[index] = matchFound[index] || m.find();

                    index++;
                }
            }

        }
        for (boolean aMatchFound : matchFound) {
            if (!aMatchFound) {
                return 0; // Didn't match all words.
            }
        }
        return 1; // Matched all words.
    }

}
