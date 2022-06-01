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
                    System.out.println("(无法识别,'" + sbb + "')");
                }
            }
        }
        System.out.println("经过词法分析后的符号串为(将数字用i替代,以便进行语法分析):");
        System.out.println(remainingInputString);
    }

    public static void makeParsingTable() {
        parsingTable.get('E').put('i', "E→TX");
        parsingTable.get('E').put('+', "ERROR");
        parsingTable.get('E').put('*', "ERROR");
        parsingTable.get('E').put('(', "E→TX");
        parsingTable.get('E').put(')', "ERROR");
        parsingTable.get('E').put('#', "ERROR");

        parsingTable.get('X').put('i', "ERROR");
        parsingTable.get('X').put('+', "X→+TX");
        parsingTable.get('X').put('*', "ERROR");
        parsingTable.get('X').put('(', "ERROR");
        parsingTable.get('X').put(')', "X→ε");
        parsingTable.get('X').put('#', "X→ε");

        parsingTable.get('T').put('i', "T→FY");
        parsingTable.get('T').put('+', "ERROR");
        parsingTable.get('T').put('*', "ERROR");
        parsingTable.get('T').put('(', "T→FY");
        parsingTable.get('T').put(')', "ERROR");
        parsingTable.get('T').put('#', "ERROR");

        parsingTable.get('Y').put('i', "ERROR");
        parsingTable.get('Y').put('+', "Y→ε");
        parsingTable.get('Y').put('*', "Y→*FY");
        parsingTable.get('Y').put('(', "ERROR");
        parsingTable.get('Y').put(')', "Y→ε");
        parsingTable.get('Y').put('#', "Y→ε");

        parsingTable.get('F').put('i', "F→i");
        parsingTable.get('F').put('+', "ERROR");
        parsingTable.get('F').put('*', "ERROR");
        parsingTable.get('F').put('(', "F→(E)");
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
        System.out.println("请输入待分析的字符串:");
        Scanner in = new Scanner(System.in);
        input = in.nextLine();
        in.close();
    }

    private static void parse() {
        System.out.println("LL(1)语法分析步骤如下:");
        Deque<Character> analysisStack = new ArrayDeque<>();
        analysisStack.add('#');
        analysisStack.add('E');
        remainingInputString.append('#');
        int len = remainingInputString.length();
        System.out.printf("%-12s%-20s%15s\t\t\t%-10s\n", "步骤", "分析栈", "余留输入串", "所用产生式");
        for (int i = 0, count = 1; i < len; count++) {
            System.out.printf("%-13s", "(" + count + ")");
            System.out.printf("%-25s%15s\t\t\t", analysisStack, remainingInputString.substring(i));
            // 分析栈栈顶符号和余留输入串首符号相同,说明匹配成功
            if (analysisStack.getLast() == remainingInputString.charAt(i)) {
                // 2个#号的匹配表示语法识别成功
                if (analysisStack.getLast() == '#') {
                    System.out.println("成功");
                    break;
                }
                else {
                    // 普通非终结符的匹配
                    analysisStack.removeLast();
                    i++;
                    System.out.println();
                    continue;
                }
            }
            // 获取LL(1)分析表中的产生式
            if (!INPUT_SYMBOL_SET.contains(remainingInputString.charAt(i))) {
                System.out.println("失败,输入符号串含非法符号!");
                break;
            }
            // 如果这个if为真,说明栈顶符号是非终结符,且和余留输入串的首符号不相等,则语法分析错误
            if (INPUT_SYMBOL_SET.contains(analysisStack.getLast())) {
                System.out.println("失败,栈顶非终结符和余留输入串首符号不匹配!");
                break;
            }
            String production = parsingTable.get(analysisStack.getLast()).get(remainingInputString.charAt(i));
            // 遇到不匹配的项,语法分析错误
            if ("ERROR".equals(production)) {
                System.out.println("失败,文法中无此产生式!");
                break;
            }
            else {
                System.out.println(production);
                // 将产生式的右部进行反转,并以此入栈
                String reversedRightPart = new StringBuilder(production.substring(2)).reverse().toString();
                int rightPartLen = reversedRightPart.length();
                analysisStack.removeLast();
                if (!"ε".equals(reversedRightPart)) {
                    for (int k = 0; k < rightPartLen; k++) {
                        analysisStack.addLast(reversedRightPart.charAt(k));
                    }
                }
            }
        }
    }

    private static void outputInfo() {
        System.out.println("本语法分析器分析的原始文法为:");
        System.out.println("E  →  E + T  |  T\n" +
                "T  →  T * F  |  F\n" +
                "F  →  ( E ) | i\n");
        System.out.println("消除左递归后的文法为:");
        System.out.println("E  →  T X\n" +
                "X  →  + T X  |  ε \n" +
                "T  →  F Y\n" +
                "Y  →  * F Y  |  ε\n" +
                "F  →  ( E )  |  i\n");
        System.out.println("该文法各非终结符的First集为:");
        System.out.println("First( E ) = { ( , i }\n" +
                "First( T ) = { ( , i }\n" +
                "First( X ) = { + , ε }\n" +
                "First( Y ) = { * , ε }\n" +
                "First( F ) = { ( , i }\n");
        System.out.println("该文法各非终结符的Follow集为:");
        System.out.println("Follow ( E ) = { ) , # }\n" +
                "Follow ( X ) = { ) , # }\n" +
                "Follow ( T ) = { ) , # , + }\n" +
                "Follow ( Y ) = { ) , # , + }\n" +
                "Follow ( F ) = { ) , # , + , * }\n");
        System.out.println("因此,该文法的LL(1)分析表为:");
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
