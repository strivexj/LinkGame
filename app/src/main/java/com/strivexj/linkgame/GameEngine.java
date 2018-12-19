package com.strivexj.linkgame;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
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
    private int[][] largeMap;
    private int row, column;
    private int[] dx = {-1, 1, 0, 0};
    private int[] dy = {0, 0, 1, -1};

    public GameEngine(int row, int column) {
        this.row = row;
        this.column = column;
        map = new int[row][column];
        fill(map, BLOCK);
        largeMap = new int[row + 2][column + 2];
    }

    public void fill(int[][] m, int value) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] = value;
            }
        }
    }

    public void eliminate(int row, int column) {
        map[row][column] = EMPTY;
    }

    public void set(int row, int column) {
        map[row][column] = BLOCK;
    }

    public boolean isGameOver() {
        boolean gameOver = true;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                if (map[i][j] == BLOCK) return false;
            }
        }
        return gameOver;
    }

    public boolean linkAble(Point start, Point end) {
        boolean linkAble = false;
        if (start.x == end.x && start.y == end.y) return false;

        fill(largeMap, EMPTY);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                largeMap[i + 1][j + 1] = map[i][j];
            }
        }

       /* List<Point> pointList = new ArrayList<>();
        pointList.add(start);*/
        Queue<Point> queue = new LinkedList<Point>();
        //设置终点为空
        largeMap[end.x + 1][end.y + 1] = EMPTY;
        //设置起点为1
        largeMap[start.x + 1][start.y + 1] = 1;

        Point p = new Point(start.x + 1, start.y + 1);
        queue.offer(p);

        while (!queue.isEmpty()) {
            Point point = queue.poll();
            if (point.x == end.x + 1 && point.y == end.y + 1) {
                linkAble = true;
                break;
            }
            //四个方向查询
            for (int i = 0; i < 4; i++) {
                int nx = point.x + dx[i], ny = point.y + dy[i];
                if (0 <= nx && nx < row + 2 && 0 <= ny && ny < column + 2 &&
                        largeMap[nx][ny] == EMPTY) {
                    queue.offer(new Point(nx, ny));
                    largeMap[nx][ny] = largeMap[point.x][point.y] + 1;
                }
            }
        }
        if (linkAble)
            getPointList(start, end, 2);
        return linkAble;
    }

    List<Point> getPointList(Point start, Point end, int maxTurnCount) {
        List<Point> pointList = new ArrayList<>();

        Point p = new Point(end.x + 1, end.y + 1);
        Queue<Point> queue = new LinkedList<Point>();
        queue.offer(p);
        //添加目标结点
        pointList.add(p);

        while (!queue.isEmpty()) {
            Point point = queue.poll();

            Log.d("map", "p x:" + point.x + " y:" + point.y);

            if (point.x == start.x + 1 && point.y == start.y + 1) {
                Log.d("map end", "p x:" + point.x + " y:" + point.y);
                break;
            }

            //TODO 四个方向查询,如果有多个选择，这里要选择拐点最少的那个方向
            for (int i = 0; i < 4; i++) {
                int nx = point.x + dx[i], ny = point.y + dy[i];
                if (0 <= nx && nx < row + 2 && 0 <= ny && ny < column + 2 &&
                        largeMap[nx][ny] != EMPTY && largeMap[nx][ny] != BLOCK && largeMap[nx][ny] + 1 == largeMap[point.x][point.y]) {
                    Point np = new Point(nx, ny);
                    queue.offer(np);
                    pointList.add(np);
                    Log.d("map new", "p x:" + np.x + " y:" + np.y);
                    break;
                }
            }
        }
        //如果只有两个结点（开始和目标结点），说明是相邻的
        List<Point> turnPointList = new ArrayList<>();
        if (pointList.size() == 2) {
            turnPointList.add(start);
            turnPointList.add(end);
            return turnPointList;
        } else {
            int turnCount = 0;
            Collections.reverse(pointList);
            turnPointList.add(start);
            //计算拐点
            for (int i = 0; i < pointList.size() - 2; i++) {
                Point p1 = pointList.get(i);
                Point p3 = pointList.get(i + 2);
                if (p1.x != p3.x && p1.y != p3.y) {
                    Point p2 = pointList.get(i + 1);
                    turnPointList.add(new Point(p2.x - 1, p2.y - 1));
                }
            }
            turnPointList.add(end);
        }


        StringBuilder sb = new StringBuilder();
        sb.append("asa  \n\n");
        for (int i = 0; i < pointList.size(); i++) {
            p = pointList.get(i);
            sb.append("第" + (i + 1) + "个点 x:" + p.x + " y:" + p.y + "\n");
        }
        Log.d("map", sb.toString());
        sb = new StringBuilder();
        sb.append("asa  \n\n");
        for (int i = 0; i < turnPointList.size(); i++) {
            p = turnPointList.get(i);
            sb.append("第" + (i + 1) + "个拐点 x:" + p.x + " y:" + p.y + "\n");
        }
        Log.d("map", sb.toString());
        return turnPointList;
    }

    public void printMap() {
        printMap(map);
        printMap(largeMap);
    }

    public void printMap(int[][] m) {
        StringBuilder sb = new StringBuilder();
        sb.append("asa  \n\n");
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                sb.append(m[i][j] + "\t");
            }
            sb.append("\n");
        }
        Log.d("map", sb.toString());
    }
}
