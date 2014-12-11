package org.metastatic.sexp4j;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Base adapter class for turning a {@link org.metastatic.sexp4j.StreamingParser}
 * into a {@link org.metastatic.sexp4j.Parser}.
 */
public abstract class ParserBase implements Parser {
    private Optional<Expression> root = Optional.absent();
    private final LinkedList<ExpressionList> stack = new LinkedList<>();
    private final StreamingParser stream;

    private class Callback implements StreamingParserCallback {
        @Override
        public void beginList() throws ParseException {
            ExpressionList newList = new ExpressionList();
            if (!stack.isEmpty())
                stack.peek().add(newList);
            else
            {
                if (root.isPresent())
                    throw new ParseException("found multiple root values");
                root = Optional.<Expression> of(newList);
            }
            stack.push(newList);
        }

        @Override
        public void endList() throws ParseException {
            if (stack.isEmpty())
                throw new ParseException("extraneous end list");
            stack.pop();
        }

        @Override
        public void onAtom(byte[] atom, Optional<byte[]> displayHint) throws ParseException {
            Atom a = new Atom(atom);
            if (displayHint.isPresent())
                a = a.withHint(new Atom(displayHint.get()));
            if (stack.isEmpty()) {
                if (root.isPresent())
                    throw new ParseException("found multiple root values");
                root = Optional.<Expression> of(a);
            }
            else
                stack.peek().add(a);
        }
    }

    /**
     * Create a new parser adapter.
     *
     * @param parser The streaming parser.
     * @throws java.lang.NullPointerException If the argument is null.
     */
    protected ParserBase(StreamingParser parser) {
        Preconditions.checkNotNull(parser);
        stream = parser;
        stream.addCallback(new Callback());
    }

    @Override
    public Expression parse() throws IOException {
        stream.parse();
        return root.orNull();
    }
}
