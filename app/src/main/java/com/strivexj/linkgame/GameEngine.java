package com.strivexj.linkgame;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

/**
 * Created by cwj on 12/4/18 18:22
 */

public class GameEngine {
    private final int EMPTY = 0;
    private final int BLOCK = -1;
    Vector<List<Point>> pointsList = new Vector<>();//存放所有可能路径的拐点
    private int[][] map;//原始布局数据
    private int[][] largeMap;//往外加多一圈空位，方便判断边缘是否可以相消
    private int row, column;

    //向上下左右移动坐标
    private int[] dx = {-1, 1, 0, 0};
    private int[] dy = {0, 0, 1, -1};

    public GameEngine(int row, int column) {
        this.row = row;
        this.column = column;
        init();
    }

    /**
     * 初始化游戏引擎
     */
    public void init() {
        pointsList.clear();
        map = new int[row][column];
        fill(map, BLOCK);
        largeMap = new int[row + 2][column + 2];
    }

    /**
     * 填充数组
     * @param m
     * @param value
     */
    public void fill(int[][] m, int value) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] = value;
            }
        }
    }

    /**
     * 消除一个元素
     * @param row
     * @param column
     */
    public void eliminate(int row, int column) {
        map[row][column] = EMPTY;
    }

    /**
     * 设置一个元素
     * @param row
     * @param column
     */
    public void set(int row, int column) {
        map[row][column] = BLOCK;
    }

    /**
     * 判断游戏是否结束
     * @return
     */
    public boolean isGameOver() {
        boolean gameOver = true;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                if (map[i][j] == BLOCK) return false;
            }
        }
        return gameOver;
    }

    /**
     * 判断两点是否可以相连,以及返回拐点坐标
     * @param start 起始点
     * @param end   目标点
     * @param maxTurnCount 最大拐点数
     * @return 不可相连则返回null
     */
    public List<Point> getLinkPoints(Point start, Point end, int maxTurnCount) {
        boolean linkable = false;
        if (start.x == end.x && start.y == end.y) return null;
        //初始化大数据
        fill(largeMap, EMPTY);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                largeMap[i + 1][j + 1] = map[i][j];
            }
        }

        Queue<Point> queue = new LinkedList<Point>();
        //设置终点为空
        largeMap[end.x + 1][end.y + 1] = EMPTY;
        //设置起点为1（代价为1）
        largeMap[start.x + 1][start.y + 1] = 1;

        //将原始坐标+1转化成大数组坐标
        Point p = new Point(start.x + 1, start.y + 1);
        queue.offer(p);

        //广度优先搜索最短路径
        while (!queue.isEmpty()) {
            Point point = queue.poll();
            if (point.x == end.x + 1 && point.y == end.y + 1) {
                linkable = true;
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
        //如果两点可以连接，计算拐点数
        if (linkable) {
            return getPointList(start, end, maxTurnCount);
        } else return null;

    }

    /**
     * 利用深度优先搜索，两点的找出路径
     * @param start
     * @param end
     */
    private void dfs(Point start, Point end) {
        //到达目标点，全部路径添加完成，新增一个列表存放新的路径
        if (start.y == end.y && start.x == end.x) {
            pointsList.add(new ArrayList<Point>());
        }
        for (int i = 0; i < 4; i++) {
            int nx = start.x + dx[i], ny = start.y + dy[i];
            if (0 <= nx && nx < row + 2 && 0 <= ny && ny < column + 2 &&
                    largeMap[nx][ny] != EMPTY && largeMap[nx][ny] != BLOCK && largeMap[nx][ny] + 1 == largeMap[start.x][start.y]) {

                //进到这个if说明出现了分支，即有多种路径,将之前的点加入新的列表
                Point np = new Point(nx, ny);
                if (pointsList.size() > 1 && pointsList.get(pointsList.size() - 1).isEmpty()) {
                    for (Point p : pointsList.get(pointsList.size() - 2)) {
                        if (largeMap[p.x][p.y] + 1 == largeMap[start.x][start.y]) {
                            break;
                        }
                        pointsList.get(pointsList.size() - 1).add(p);
                    }
                }
                pointsList.get(pointsList.size() - 1).add(np);
                dfs(np, end);
            }
        }
    }

    /**
     * 将路径上的点转化成拐点
     * @param start
     * @param end
     * @param maxTurnCount
     * @return
     */
    public List<Point> getPointList(Point start, Point end, int maxTurnCount) {
        pointsList.clear();
        pointsList.add(new LinkedList<Point>());

        //先将全部可行路径找出来，存放到pointsList
        dfs(new Point(end.x + 1, end.y + 1), new Point(start.x + 1, start.y + 1));

        for (int i = 0; i < pointsList.size(); i++) {
            StringBuilder sb = new StringBuilder();
            if (pointsList.get(i).isEmpty()) break;
            sb.append("getPointList2  \n 第" + i + "种\n");
            //反转路径，因为之前是从尾往首找的路径
            Collections.reverse(pointsList.get(i));
            //添加目标点
            pointsList.get(i).add(new Point(end.x + 1, end.y + 1));

            //如果只有两个结点（开始和目标结点），说明是相邻的
            List<Point> turnPointList = new ArrayList<>();
            if (pointsList.get(i).size() == 2) {
                turnPointList.add(start);
                turnPointList.add(end);
                return turnPointList;
            } else {
                turnPointList.add(start);
                //计算拐点
                for (int j = 0; j < pointsList.get(i).size() - 2; j++) {
                    Point p1 = pointsList.get(i).get(j);
                    Point p3 = pointsList.get(i).get(j + 2);
                    if (p1.x != p3.x && p1.y != p3.y) {
                        Point p2 = pointsList.get(i).get(j + 1);
                        turnPointList.add(new Point(p2.x - 1, p2.y - 1));
                    }
                }
                turnPointList.add(end);
            }

            for (int j = 0; j < turnPointList.size(); j++) {
                Point p = turnPointList.get(j);
                sb.append("第" + (j + 1) + "个拐点 x:" + p.x + " y:" + p.y + "\n");
            }
            Log.d("getPointList2", sb.toString());
            //加2是因为算上了开始和目标结点
            if (turnPointList.size() > maxTurnCount + 2) {
                continue;
            } else return turnPointList;
        }
        return null;
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
        Log.d("getPointList2", sb.toString());
    }
}
