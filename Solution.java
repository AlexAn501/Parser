public class Solution {
    public static void main(String[] args) {
        Solution solution = new Solution();
        solution.recurse("sin(2*(  -5+1.5*4)+28)", 0);
    }

    public void recurse(final String expression, int countOperation) {
        //implement

//        рекурсия притянута за уши чтобы угодить вале.

        String str = "";
        int count = countOperation;
        if(expression.contains(" ")){
            str = expression.replaceFirst(" ","");
        }else {
            NumberFormat format = new DecimalFormat("#.##");
            List<Lexeme> lexemes = lexAnalyze(expression);

            LexemeBuffer lexemeBuffer = new LexemeBuffer(lexemes);
            double res = expr(lexemeBuffer);

            Lexeme lexeme = lexemeBuffer.countOp();

            String out = format.format(res).replace(',', '.');
            System.out.printf("%s %s%n", out, lexeme.value);
            return;
        }
        recurse(str,count);
    }

    private enum LexemeType {
        LEFT_BRACKET, RIGHT_BRACKET,
        OP_PLUS, OP_MINUS, OP_MUL, OP_DIV,
        NUMBER,
        SIN, COS, TAN,
        POW,
        EOF,
        COUNT_OP;
    }

    private class Lexeme {
        private LexemeType type;
        private String value;

        public Lexeme(LexemeType type, String value) {
            this.type = type;
            this.value = value;
        }

        public Lexeme(LexemeType type, Character value) {
            this.type = type;
            this.value = value.toString();
        }

        @Override
        public String toString() {
            return "Lexeme{" +
                    "type=" + type +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    private class LexemeBuffer {
        private int pos;

        private List<Lexeme> lexemes;

        public LexemeBuffer(List<Lexeme> lexemes) {
            this.lexemes = lexemes;
        }

        public Lexeme next() {
            return lexemes.get(pos++);
        }

        public void back() {
            pos--;
        }

        public int getPos() {
            return pos;
        }

        public Lexeme countOp() {
            return lexemes.get(lexemes.size() - 1);
        }
    }

    private List<Lexeme> lexAnalyze(String expr) {
        List<Lexeme> lexemes = new ArrayList<>();
        int pos = 0;
        int count = 0;

        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            switch (c) {
                case '(':
                    lexemes.add(new Lexeme(LexemeType.LEFT_BRACKET, c));
                    pos++;
                    continue;
                case ')':
                    lexemes.add(new Lexeme(LexemeType.RIGHT_BRACKET, c));
                    pos++;
                    continue;
                case '*':
                    lexemes.add(new Lexeme(LexemeType.OP_MUL, c));
                    pos++;
                    count++;
                    continue;
                case '/':
                    lexemes.add(new Lexeme(LexemeType.OP_DIV, c));
                    pos++;
                    count++;
                    continue;
                case '+':
                    lexemes.add(new Lexeme(LexemeType.OP_PLUS, c));
                    pos++;
                    count++;
                    continue;
                case '-':
                    lexemes.add(new Lexeme(LexemeType.OP_MINUS, c));
                    pos++;
                    count++;
                    continue;
                case '^':
                    lexemes.add(new Lexeme(LexemeType.POW, c));
                    pos++;
                    count++;
                    continue;
                default:
                    if (c <= '9' && c >= '0' || c == '.') {
                        StringBuilder sb = new StringBuilder();
                        do {
                            sb.append(c);
                            pos++;
                            if (pos >= expr.length()) {
                                break;
                            }
                            c = expr.charAt(pos);
                        } while (c <= '9' && c >= '0' || c == '.');
                        lexemes.add(new Lexeme(LexemeType.NUMBER, sb.toString()));

                    } else if (c >= 'a' && c <= 'z'
                            || c >= 'A' && c <= 'Z') {
                        StringBuilder sb = new StringBuilder();
                        do {
                            sb.append(c);
                            pos++;
                            if (pos >= expr.length()) {
                                break;
                            }
                            c = expr.charAt(pos);
                        } while (c >= 'a' && c <= 'z'
                                || c >= 'A' && c <= 'Z');

                        LexemeType type = LexemeType.valueOf(sb.toString().toUpperCase());

                        switch (type) {
                            case COS:
                                lexemes.add(new Lexeme(LexemeType.COS, sb.toString()));
                                count++;
                                continue;
                            case SIN:
                                lexemes.add(new Lexeme(LexemeType.SIN, sb.toString()));
                                count++;
                                continue;
                            case TAN:
                                lexemes.add(new Lexeme(LexemeType.TAN, sb.toString()));
                                count++;
                                continue;
                            default:
                                throw new RuntimeException("Unsupported function " + sb);
                        }
                    } else {
                        if (c != ' ') {
                            throw new RuntimeException("Unexpected character: " + c);
                        }
                        pos++;
                    }
            }
        }

        lexemes.add(new Lexeme(LexemeType.EOF, ""));
        lexemes.add(new Lexeme(LexemeType.COUNT_OP, String.valueOf(count)));
        return lexemes;
    }

    private double expr(LexemeBuffer lexemeBuffer) {
        Lexeme lexeme = lexemeBuffer.next();
        if (lexeme.type == LexemeType.EOF) {
            return 0;
        } else {
            lexemeBuffer.back();
            return parseFormat(plusMinus(lexemeBuffer));
        }
    }

    private double plusMinus(LexemeBuffer lexemeBuffer) {
        double value = multDiv(lexemeBuffer);
        while (true) {
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.type) {
                case OP_PLUS:
                    value += multDiv(lexemeBuffer);
                    continue;
                case OP_MINUS:
                    value -= multDiv(lexemeBuffer);
                    continue;
                case EOF: case RIGHT_BRACKET:
                    lexemeBuffer.back();
                    return parseFormat(value);
                default:
                    throw new RuntimeException("Unexpected token: " + lexeme.value
                            + " at position: " + lexemeBuffer.getPos());

            }
        }
    }

    private double multDiv(LexemeBuffer lexemeBuffer) {
        double value = pow(lexemeBuffer);
        while (true) {
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.type) {
                case OP_MUL:
                    value *= pow(lexemeBuffer);
                    continue;
                case OP_DIV:
                    value /= pow(lexemeBuffer);
                    continue;
                case EOF: case RIGHT_BRACKET: case OP_PLUS: case OP_MINUS:
                    lexemeBuffer.back();
                    return parseFormat(value);
                default:
                    throw new RuntimeException("Unexpected lexeme: " + lexeme.value
                            + " at position: " + lexemeBuffer.getPos());
            }
        }
    }

    private double pow(LexemeBuffer lexemeBuffer) {
        double value = factor(lexemeBuffer);
        while (true) {
            Lexeme lexeme = lexemeBuffer.next();

            switch (lexeme.type) {
                case POW:
                    value = Math.pow(value, factor(lexemeBuffer));
                    continue;
                default:
                    lexemeBuffer.back();
                    return parseFormat(value);

            }
        }
    }

    private double factor(LexemeBuffer lexemeBuffer) {
        Lexeme lexeme = lexemeBuffer.next();
        switch (lexeme.type) {
            case OP_MINUS:
                return -parseFormat(pow(lexemeBuffer));
            case SIN: case COS: case TAN:
                lexemeBuffer.back();
                return func(lexemeBuffer);
            case NUMBER:
                double valueN = Double.parseDouble(lexeme.value);
                return parseFormat(valueN);
            case LEFT_BRACKET:
                double value = expr(lexemeBuffer);
                lexeme = lexemeBuffer.next();

                if (lexeme.type != LexemeType.RIGHT_BRACKET) {
                    throw new RuntimeException("Unexpected lexeme: " + lexeme.value
                            + " at position: " + lexemeBuffer.getPos());
                }
                return parseFormat(value);

            default:
                throw new RuntimeException("Unexpected lexeme: " + lexeme.value
                    + " at position: " + lexemeBuffer.getPos());
        }
    }

    private double func(LexemeBuffer lexemeBuffer) {
        LexemeType type = lexemeBuffer.next().type;
        Lexeme lexeme = lexemeBuffer.next();

        if (lexeme.type != LexemeType.LEFT_BRACKET) {
            throw new RuntimeException("Wrong function call syntax at " + lexeme.value);
        }

        switch (type) {
            case SIN:
                double sin = Math.sin(Math.toRadians(expr(lexemeBuffer)));
                lexemeBuffer.next();
                return parseFormat(sin);
            case COS:
                double cos = Math.cos(Math.toRadians(expr(lexemeBuffer)));
                lexemeBuffer.next();
                return parseFormat(cos);
            case TAN:
                double tan = Math.tan(Math.toRadians(expr(lexemeBuffer)));
                lexemeBuffer.next();
                return parseFormat(tan);
            default:
                throw new RuntimeException("Unsupported operation " + lexeme.value);
        }
    }

    private double parseFormat(double value) {
        NumberFormat format = new DecimalFormat("#.##");
        String afterFormat = format.format(value);
        afterFormat = afterFormat.replace(',', '.');
        return Double.parseDouble(afterFormat);
    }

    public Solution() {
        //don't delete
    }
}
