package org.example;
import java.io.*;
import java.util.*;

//        Правила игры:
//            Система загадывает случайное слово,
//                пишет на бумаге любые две буквы слова и отмечает места для остальных букв, например чертами
//                Также рисуется виселица с петлёй
//
//            Согласно традиции русских лингвистических игр, слово должно быть именем существительным, нарицательным
//                в именительном падеже единственного числа, либо множественного числа при отсутствии у слова формы единственного числа.
//            Второй игрок предлагает букву, которая может входить в это слово. Если такая буква есть в слове, то первый игрок пишет
//                её над соответствующими этой букве чертами — столько раз, сколько она встречается в слове. Если такой буквы нет,
//                то к виселице добавляется круг в петле, изображающий голову. Второй игрок продолжает отгадывать буквы до тех пор,
//                пока не отгадает всё слово. За каждый неправильный ответ первый игрок добавляет одну часть туловища к виселице
//                (обычно их 6: голова, туловище, 2 руки и 2 ноги, существует также вариант с 8 частями — добавляются ступни,
//                а также самый длинный вариант, когда сначала за неотгаданную букву рисуются части самой виселицы).
//            Если туловище в виселице нарисовано полностью, то отгадывающий игрок проигрывает, считается повешенным.
//                Если игроку удаётся угадать слово, он выигрывает.
//
//        Функционал приложения и меню консольного интерфейса
//        1) При старте, приложение предлагает начать новую игру или выйти из приложения
//        2) При начале новой игры, случайным образом загадывается слово, и игрок начинает процесс по его отгадыванию
//        3) После каждой введенной буквы выводим в консоль счётчик ошибок, текущее состояние виселицы (нарисованное ASCII символами)
//        4) По завершении игры выводим результат (победа или поражение) и возвращаемся к состоянию #1 - предложение начать новую игру или выйти из приложения
//
//        План работы над приложением
//        1) Найти в интернете словарь существительных в именительным падеже, отбросить из него слишком короткие слова. Этот словарь будет источником для выбора случайного загаданного слова для каждого раунда игры
//        2) Реализовать игровой цикл отгадывания букв и отображения текущего состояния виселицы
//        3) Реализовать цикл по перезапуску игры после победы/поражения
//
//
//        Чеклист для самопроверки(косяки, которые решил оставить):
//           Использование массивов. Лучше применять коллекции List<>, Set<> - Имеет место, но не является критичным. Решил не переписывать



public class Hangman {

    private static  Integer errorsPerCurrentRound;
    private static String word;
    private static String currentWordMask;
    private static String guessedLetters;
    private static String enteredLetters;
    private static final Random rnd = new Random();

    private static final Scanner scanner = new Scanner(System.in);
    private static final int minWordLength = 6;
    private static final int maxErrorsPerRound = 6;
    private static final String[] visualisationsOfStates = new String[]{
        "*----*\n" +
        "     |\n" +
        "     |\n" +
        "     |\n" +
        "   =====\n",

        " *----*\n" +
        " O    |\n" +
        "      |\n" +
        "      |\n" +
        "    =====\n",

        " *----*\n" +
        " O    |\n" +
        " |    |\n" +
        "      |\n" +
        "    =====\n",

        " *----*\n" +
        " O    |\n" +
        "/|    |\n" +
        "      |\n" +
        "    =====\n",
        " *----*\n" +
        " O    |\n" +
        "/|\\   |\n" +
        "      |\n" +
        "    ===== \n",

        "*----*\n" +
        " O   |\n" +
        "/|\\  |\n" +
        "/    |\n" +
        "    =====",

        "*----*\n" +
        " O   |\n" +
        "/|\\  |\n" +
        "/ \\  |\n" +
        "    ====\n",

    };

    public static void main(String[] args) {
        startGame();
    }

    public static void startGame() {
        String[] dictionary = loadDictionary();

        while (true) {
            boolean userWantPlay=checkUserWantPlay();
            if(userWantPlay){
                word=dictionary[rnd.nextInt(dictionary.length)];
                startGameRound();
            }
            else{
                scanner.close();
                return;
            }
        }
    }

    public static String[] loadDictionary() {
        String[] dictionary = new String[]{};
        try {
            File file = new File("dictionary.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            List<String> lines = new LinkedList<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() >= Hangman.minWordLength)
                    lines.add(line);
            }
            reader.close();
            dictionary = lines.toArray(new String[]{});
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    public static void startGameRound() {
        errorsPerCurrentRound=0;
        currentWordMask=word.replaceAll(".","_");
        guessedLetters="";
        enteredLetters="";
        System.out.println("--------------");
        System.out.println("Начало Раунда");
        System.out.println(word);
        do{
            startGameLoop();
        }while (errorsPerCurrentRound<maxErrorsPerRound && currentWordMask.contains("_"));

        showGameResult();
    }
    public static void startGameLoop() {
        drawStateVisualisation();
        String inpLetter=userInputSymbol( "Введите букву русского алфавита", "[\\u0400-\\u04FF]",
                "Введена не буква русского алфавита, повторите ввод\n",true);

        if(word.contains(inpLetter)){
            guessedLetters+=inpLetter;
            currentWordMask=word.replaceAll("[^"+guessedLetters+"]","_");
        }else{
            System.out.println("Буква отсутствует в слове");
            errorsPerCurrentRound+=1;
        }
    }

    public static void drawStateVisualisation() {
        System.out.println(visualisationsOfStates[errorsPerCurrentRound]);
        System.out.println(currentWordMask);
        System.out.printf("Ошибок %d/%d\n",errorsPerCurrentRound,maxErrorsPerRound);
    }
    public static boolean checkUserWantPlay(){
        String yOrN=userInputSymbol( "Если хотите начать игру - введите y. Если хотите выйти - введите n",
                "[y|n]", "Символ не y и не n, повторите ввод\n",false);
        return yOrN.equals("y");
    }
    public static String userInputSymbol(String firstMessageForUser,String regexForMatchSymbol,String messageRegexNotMatch, boolean needCheckIsEnteredEarlier){
        System.out.println(firstMessageForUser);
        do {
            String letter = scanner.nextLine();
            if(letter.length()>0){
                letter=letter.substring(0,1).toLowerCase();
                System.out.printf("Введен символ: %s \n", letter);

                if(needCheckIsEnteredEarlier && enteredLetters.contains(letter)){
                    System.out.printf("Символ ранее уже был введён. Введены были символы:\""+enteredLetters+"\"\n");
                }
                else{
                    if(letter.matches(regexForMatchSymbol)){
                        enteredLetters+=letter;
                        return letter;
                    }
                    else{
                        System.out.printf(messageRegexNotMatch);
                    }
                }

            }else{
                System.out.printf("Ввод пуст, повторите ввод\n");
            }

        }while(true);
    }

    public static void showGameResult(){
        drawStateVisualisation();
        if(errorsPerCurrentRound<maxErrorsPerRound){
            System.out.println("Вы победили!");
        }else{
            System.out.println("Вы проиграли!");
        }
    }
}
