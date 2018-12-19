package com.strivexj.linkgame;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by cwj on 12/4/18 18:22
 */
public class GameEngine {
    private final int EMPTY = 0;
    private final int BLOCK = -1;
    private int[][] map;
    private int[][] tempMap;
    private int row, column;

    public GameEngine(int row, int column) {
        this.row = row;
        this.column = column;
        map = new int[row][column];
        Arrays.fill(map, BLOCK);
    }

    public void eliminate(int row, int column) {
        map[row][column] = EMPTY;
    }

    public boolean linkAble(Point start, Point end) {
        boolean linkAble = true;
        tempMap = new int[row + 2][column + 2];
        Arrays.fill(tempMap, EMPTY);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                tempMap[i + 1][j + 1] = map[i][j];
            }
        }

        List<Point> pointList = new ArrayList<>();
        pointList.add(start);
        Queue<Point> queue = new LinkedList<Point>();
        //设置终点为空
        tempMap[end.x + 1][end.y + 1] = EMPTY;
        queue.offer(start);
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (!queue.isEmpty()) {
            Point point = queue.poll();
            if (point.x == end.x + 1 && point.y == end.y + 1) {
                pointList.add(end);
                printMap();
                break;
            }

            //四个方向查询
            for (int i = 0; i < 4; i++) {
                int nx = point.x + dx[i], ny = point.y + dy[i];
                if (0 <= nx && nx < row + 2 && 0 <= ny && ny < column + 2 &&
                        tempMap[nx][ny] == EMPTY) {
                    queue.offer(new Point(nx, ny));
                    tempMap[nx][ny] = tempMap[point.x][point.y];
                }
            }
        }

        return linkAble;
    }

    private void printMap() {
        for (int i = 0; i < row + 2; i++) {
            for (int j = 0; j < column + 2; j++) {
                System.out.println(tempMap[i][j] + " ");
            }
            System.out.println();
        }
    }

}
