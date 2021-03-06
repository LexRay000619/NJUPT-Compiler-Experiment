package compiler;

import java.util.*;

/**
 * @author Lex
 * @create 2022-05-30-13:35
 */
public class Parser {

    private static final List<Character> INPUT_SYMBOL_SET = Arrays.asList('(', 'i', ')', '*', '+', '#');
    private static Map<Character, Map<Character, String>> parsingTable;
    private static String input;
    private static List<String> keyWords;
    private static List<Character> separators;
    private static List<String> operators;
    private static StringBuilder sb;
    private static StringBuilder remainingInputString;

    private static void initLexicalString() {
        keyWords = Arrays.asList(
                "auto", "break", "case", "char", "const", "continue", "default", "do",
                "double", "else", "enum", "extern", "float", "for", "goto", "if",
                "int", "long", "register", "return", "short", "signed", "sizeof", "static",
                "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while");
        separators = Arrays.asList(
                '(', ')', '{', '}', ';', ',', '.',
                ':', '\'', '\"', '[', ']', '#', '_');
        operators = Arrays.asList(
                "=", "==", "+", "-", "*", "/", "+=", "-=", "*=",
                "/=", "++", "--", ">=", "<=", "!=", "!", "%", "%=",
                "&", "&&", "|", "||", "?:", "~", "^", "<<", ">>"
        );
        String s = input.replaceAll("//.+", "").replaceAll("\\s", "").replaceAll("/\\*.*?\\*/", "");
        sb = new StringBuilder(s);
        remainingInputString = new StringBuilder();
    }

    public static void initParsingTable() {
        parsingTable = new HashMap<>(5);
        parsingTable.put('E', new HashMap<>(6));
        parsingTable.put('X', new HashMap<>(6));
        parsingTable.put('T', new HashMap<>(6));
        parsingTable.put('Y', new HashMap<>(6));
        parsingTable.put('F', new HashMap<>(6));
    }

    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isLetter(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    private static boolean isSeparator(char ch) {
        return separators.contains(ch);
    }

    private static boolean isKeyWord(String s) {
        return keyWords.contains(s);
    }

    private static void lexicalAnalyze() {
        char ch;
        StringBuilder sbb;
        for (int i = 0; i < sb.length(); i++) {
            ch = sb.charAt(i);
            sbb = new StringBuilder();
            if (isLetter(ch)) {
                do {
                    sbb.append(ch);
                    if (++i < sb.length()) {
                        ch = sb.charAt(i);
                    }
                    else {
                        break;
                    }
                } while ((isLetter(ch) || isDigit(ch) || ch == '_') && !(isKeyWord(sbb.toString())));
                i--;
                remainingInputString.append('i');
            }
            else if (isDigit(ch)) {
                do {
                    sbb.append(ch);
                    if (++i < sb.length()) {
                        ch = sb.charAt(i);
                    }
                    else {
                        break;
                    }
                } while (isDigit(ch));
                i--;
                remainingInputString.append('i');
            }
            else if (isSeparator(ch)) {
                remainingInputString.append(ch);
            }
            else {
                do {
                    sbb.append(ch);
                    if (++i < sb.length()) {
                        ch = sb.charAt(i);
                    }
                    else {
                        break;
                    }
                } while (!(isDigit(ch) || isLetter(ch) || isSeparator(ch)));
                i--;
                if (operators.contains(sbb.toString())) {
                    remainingInputString.append(sbb);
                }
                else {
                    System.out.println("(????????????,'" + sbb + "')");
                }
            }
        }
        System.out.println("????????????????????????????????????(????????????i??????,????????????????????????):");
        System.out.println(remainingInputString);
    }

    public static void makeParsingTable() {
        parsingTable.get('E').put('i', "E???TX");
        parsingTable.get('E').put('+', "ERROR");
        parsingTable.get('E').put('*', "ERROR");
        parsingTable.get('E').put('(', "E???TX");
        parsingTable.get('E').put(')', "ERROR");
        parsingTable.get('E').put('#', "ERROR");

        parsingTable.get('X').put('i', "ERROR");
        parsingTable.get('X').put('+', "X???+TX");
        parsingTable.get('X').put('*', "ERROR");
        parsingTable.get('X').put('(', "ERROR");
        parsingTable.get('X').put(')', "X?????");
        parsingTable.get('X').put('#', "X?????");

        parsingTable.get('T').put('i', "T???FY");
        parsingTable.get('T').put('+', "ERROR");
        parsingTable.get('T').put('*', "ERROR");
        parsingTable.get('T').put('(', "T???FY");
        parsingTable.get('T').put(')', "ERROR");
        parsingTable.get('T').put('#', "ERROR");

        parsingTable.get('Y').put('i', "ERROR");
        parsingTable.get('Y').put('+', "Y?????");
        parsingTable.get('Y').put('*', "Y???*FY");
        parsingTable.get('Y').put('(', "ERROR");
        parsingTable.get('Y').put(')', "Y?????");
        parsingTable.get('Y').put('#', "Y?????");

        parsingTable.get('F').put('i', "F???i");
        parsingTable.get('F').put('+', "ERROR");
        parsingTable.get('F').put('*', "ERROR");
        parsingTable.get('F').put('(', "F???(E)");
        parsingTable.get('F').put(')', "ERROR");
        parsingTable.get('F').put('#', "ERROR");
    }

    public static void main(String[] args) {
        initParsingTable();
        makeParsingTable();
        outputInfo();
        input();
        initLexicalString();
        lexicalAnalyze();
        parse();
    }

    private static void input() {
        System.out.println("??????????????????????????????:");
        Scanner in = new Scanner(System.in);
        input = in.nextLine();
        in.close();
    }

    private static void parse() {
        System.out.println("LL(1)????????????????????????:");
        Deque<Character> analysisStack = new ArrayDeque<>();
        analysisStack.add('#');
        analysisStack.add('E');
        remainingInputString.append('#');
        int len = remainingInputString.length();
        System.out.printf("%-12s%-20s%15s\t\t\t%-10s\n", "??????", "?????????", "???????????????", "???????????????");
        for (int i = 0, count = 1; i < len; count++) {
            System.out.printf("%-13s", "(" + count + ")");
            System.out.printf("%-25s%15s\t\t\t", analysisStack, remainingInputString.substring(i));
            // ??????????????????????????????????????????????????????,??????????????????
            if (analysisStack.getLast() == remainingInputString.charAt(i)) {
                // 2???#????????????????????????????????????
                if (analysisStack.getLast() == '#') {
                    System.out.println("??????");
                    break;
                }
                else {
                    // ???????????????????????????
                    analysisStack.removeLast();
                    i++;
                    System.out.println();
                    continue;
                }
            }
            // ??????LL(1)????????????????????????
            if (!INPUT_SYMBOL_SET.contains(remainingInputString.charAt(i))) {
                System.out.println("??????,??????????????????????????????!");
                break;
            }
            // ????????????if??????,?????????????????????????????????,??????????????????????????????????????????,?????????????????????
            if (INPUT_SYMBOL_SET.contains(analysisStack.getLast())) {
                System.out.println("??????,??????????????????????????????????????????????????????!");
                break;
            }
            String production = parsingTable.get(analysisStack.getLast()).get(remainingInputString.charAt(i));
            // ?????????????????????,??????????????????
            if ("ERROR".equals(production)) {
                System.out.println("??????,????????????????????????!");
                break;
            }
            else {
                System.out.println(production);
                // ?????????????????????????????????,???????????????
                String reversedRightPart = new StringBuilder(production.substring(2)).reverse().toString();
                int rightPartLen = reversedRightPart.length();
                analysisStack.removeLast();
                if (!"??".equals(reversedRightPart)) {
                    for (int k = 0; k < rightPartLen; k++) {
                        analysisStack.addLast(reversedRightPart.charAt(k));
                    }
                }
            }
        }
    }

    private static void outputInfo() {
        System.out.println("??????????????????????????????????????????:");
        System.out.println("E  ???  E + T  |  T\n" +
                "T  ???  T * F  |  F\n" +
                "F  ???  ( E ) | i\n");
        System.out.println("??????????????????????????????:");
        System.out.println("E  ???  T X\n" +
                "X  ???  + T X  |  ?? \n" +
                "T  ???  F Y\n" +
                "Y  ???  * F Y  |  ??\n" +
                "F  ???  ( E )  |  i\n");
        System.out.println("???????????????????????????First??????:");
        System.out.println("First( E ) = { ( , i }\n" +
                "First( T ) = { ( , i }\n" +
                "First( X ) = { + , ?? }\n" +
                "First( Y ) = { * , ?? }\n" +
                "First( F ) = { ( , i }\n");
        System.out.println("???????????????????????????Follow??????:");
        System.out.println("Follow ( E ) = { ) , # }\n" +
                "Follow ( X ) = { ) , # }\n" +
                "Follow ( T ) = { ) , # , + }\n" +
                "Follow ( Y ) = { ) , # , + }\n" +
                "Follow ( F ) = { ) , # , + , * }\n");
        System.out.println("??????,????????????LL(1)????????????:");
        System.out.printf("%-12s", "");
        for (Character c : INPUT_SYMBOL_SET) {
            System.out.printf("%-12c", c);
        }
        System.out.println();
        for (Character key : parsingTable.keySet()) {
            Map<Character, String> value = parsingTable.get(key);
            System.out.printf("%-12s", key);
            for (String s : value.values()) {
                if ("ERROR".equals(s)) {
                    s = "";
                }
                System.out.printf("%-12s", s);
            }
            System.out.println();
        }
        System.out.println();
    }
}
