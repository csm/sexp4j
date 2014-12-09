package org.metastatic.sexp4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Optional;

public abstract class ParserBase implements Parser {
    private Optional<Expression> root = Optional.empty();
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
                root = Optional.of(newList);
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
        public void onAtom(byte[] atom) throws ParseException {
            if (stack.isEmpty()) {
                if (root.isPresent())
                    throw new ParseException("found multiple root values");
                root = Optional.of(new Atom(atom));
            }
            else
                stack.peek().add(new Atom(atom));
        }
    }

    protected ParserBase(StreamingParser parser) {
        stream = parser;
        stream.addCallback(new Callback());
    }

    public Expression parse() throws IOException {
        stream.parse();
        return root.orElse(null);
    }
}