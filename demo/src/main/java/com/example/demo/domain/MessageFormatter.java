package com.example.demo.domain;

public class MessageFormatter {
    public static String getFormattedMessage(String message, int lineLength) {
        if (message.length() < 40)
            return message;
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        while ( i < message.length() ) {
            int endLine = Math.min(i + lineLength, message.length());
            int space = 0;

            for (int j = i; j < endLine; j++)
                if (message.charAt(j) == ' ')
                    space = j;

            if (space != 0 && space > endLine - 8) {
                stringBuilder.append(message,i, space).append("\n");
                i = space + 1;

            } else {
                if (endLine < message.length()) {
                    stringBuilder.append(message, i, endLine - 1).append("\n");
                    i = endLine - 1;

                } else {
                    stringBuilder.append(message, i, endLine);
                    i = endLine;
                }
            }
        }
        return stringBuilder.toString();
    }
}
