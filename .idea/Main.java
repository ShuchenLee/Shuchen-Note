import java.util.*;
public class Main{
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        int N = sc.nextInt(),T = sc.nextInt();
        int[][] data = new int[N+1][2];
        for(int i=1;i<=N;i++){
            data[i][0] = sc.nextInt();
            data[i][1] = sc.nextInt();
        }
        int res = Integer.MAX_VALUE;
        int scoreSum = 0;
        int difSum = 0;
        int left = 1,right = 1;
        while(right <= N){
            scoreSum += data[right][0];
            difSum += data[right][1];
            while(scoreSum > T && left <= right){
                res = Math.min(res,difSum);
                scoreSum -= data[left][0];
                difSum -= data[left][1];
                left++;
            }
            right ++;
        }
        System.out.println(res);

    }
}