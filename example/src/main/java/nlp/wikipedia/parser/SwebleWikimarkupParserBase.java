/**
 * This file is part of Wikiforia.
 *
 * Wikiforia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wikiforia is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package nlp.wikipedia.parser;

import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import nlp.mediawiki.model.Page;
import nlp.pipeline.Mapper;
import nlp.wikipedia.lang.TemplateConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * The Sweble parser base
 */
public abstract class SwebleWikimarkupParserBase<T> extends Mapper<Page,T,Page> {
    protected final WikiConfig config;

    public SwebleWikimarkupParserBase(TemplateConfig config) {
        this.config = config.get();
    }

    /**
     * Extract information from the page
     * @param page the page
     * @param cp the processed page that contains the AST
     * @return instance of T
     */
    protected T extract(Page page, EngProcessedPage cp) {
        throw new UnsupportedOperationException();
    }

    public T parse(Page page) {
        try {
            return extract(page, SwebleParserUtil.parsePage(
                    config,
                    page.getTitle(),
                    page.getRevision(),
                    page.getContent())
            );
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public WikiConfig getConfig() {
        return config;
    }

    @Override
    public void process(List<Page> batch) {
        ArrayList<T> mapped = new ArrayList<T>(batch.size());
        ArrayList<Page> failed = new ArrayList<Page>();

        for(Page page : batch) {
            if(page.getFormat().equals("text/x-wiki")) {

                try {
                    mapped.add(parse(page));
                } catch (Exception ex) {
                    failed.add(page);
                }
            }
            else
            {
                //Failed because it is not of x-wiki format.
                log(page.getTitle() + " ignored because it is not wikimarkup, has format " + page.getFormat());
            }
        }

        output(mapped);

        if(failed.size() > 0)
            error(failed);
    }

}
