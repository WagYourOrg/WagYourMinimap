package xyz.wagyourtail.config;

public record Or<T, U>(T t, U u) {
    public static void main(String[] args) {
        Object[] a = new Object[2];
        a[0] = 1;
        a[1] = 2.0f;
        System.out.println(a[0]);
        System.out.println(a[1]);

        Or<Integer, Float> or = new Or<>(1, 2.0f);
        System.out.println(or);

        Test test = new Test(1, "2,=", '3');
        System.out.println(test);
    }

    public record Test(int a, String b, char c) {

    }
}
