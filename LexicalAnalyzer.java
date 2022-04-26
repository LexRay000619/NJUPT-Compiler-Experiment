package compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lex
 * @create 2022-04-25-9:51
 */
public class LexicalAnalyzer {
    List<String> keyWords;
    List<Character> separators;
    List<String> operators;
    StringBuilder sb;
    FileWriter fw;

    public static void main(String[] args) throws Exception {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
        lexicalAnalyzer.init();
        lexicalAnalyzer.solution();
    }

    private void init() throws Exception {
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
        fw = new FileWriter("D:\\afterCompile.txt", false);
        Path path = Paths.get("D:\\code.c");
        byte[] data = Files.readAllBytes(path);
        System.out.println("源程序代码为：\n" + new String(data, StandardCharsets.UTF_8));
        fw.write("源程序代码为：\n" + new String(data, StandardCharsets.UTF_8) + "\n");
//        下面这行代码从逻辑上、测试工具上都能成功识别所有空格、换行符、制表符、多种注释形式并替换，
//        但是在Java代码中，却无法替换掉带有换行的多行注释，应该是Java语言的问题
//        String s=new String(data,StandardCharsets.UTF_8).replaceAll("//.+|/\\*(.|\\n)*?\\*/|\\s","");
        // 因此采用链式写法，分步解决问题,减少上一版代码的冗余中间量
        String s = new String(data, StandardCharsets.UTF_8).replaceAll("//.+", "").replaceAll("\\s", "").replaceAll("/\\*.*?\\*/", "");
        sb = new StringBuilder(s);
        System.out.println("经过预处理后的源程序为：\n" + sb);
        fw.write("经过预处理后的源程序为：\n" + sb);
        fw.write("\n");
    }

    boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    boolean isLetter(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    boolean isSeparator(char ch) {
        return separators.contains(ch);
    }

    boolean isKeyWord(String s) {
        return keyWords.contains(s);
    }


    void solution() throws IOException {
        System.out.println("词法分析结果为：");
        fw.write("词法分析结果为：\n");
        char ch;
        StringBuilder sbb;
        for (int i = 0; i < sb.length(); i++) {
            ch = sb.charAt(i);
            sbb = new StringBuilder();
            if (isLetter(ch)) {
                do {
                    sbb.append(ch);
                    ch = sb.charAt(++i);
                } while ((isLetter(ch) || isDigit(ch) || ch == '_') && !(isKeyWord(sbb.toString())));
                i--;
                if (isKeyWord(sbb.toString())) {
                    System.out.println("(关键字,'" + sbb + "')");
                    fw.write("(关键字,'" + sbb + "')\n");
                }
                else {
                    System.out.println("(标识符,'" + sbb + "')");
                    fw.write("(标识符,'" + sbb + "')\n");
                }
            }
            else if (isDigit(ch)) {
                do {
                    sbb.append(ch);
                    ch = sb.charAt(++i);
                } while (isDigit(ch));
                i--;
                System.out.println("(整数,'" + sbb + "')");
                fw.write("(整数,'" + sbb + "')\n");
            }
            else if (isSeparator(ch)) {
                System.out.println("(分隔符,'" + ch + "')");
                fw.write("(分隔符,'" + ch + "')\n");
            }
            else {
                do {
                    sbb.append(ch);
                    ch = sb.charAt(++i);
                } while (!(isDigit(ch) || isLetter(ch) || isSeparator(ch)));
                i--;
                if (operators.contains(sbb.toString())) {
                    System.out.println("(运算符,'" + sbb + "')");
                    fw.write("(运算符,'" + sbb + "')\n");
                }
                else {
                    System.out.println("(无法识别,'" + sbb + "')");
                    fw.write("(无法识别,'" + sbb + "')\n");
                }
            }
        }
        fw.close();
    }
}
