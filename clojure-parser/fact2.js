var a = 10;
var b = 'hello';
var c = null;
var d = a;
var e = 1 + 2 + 3 + 4;
var f = {
    'a': 1,
    'b': 2,
    'c': 3
};
var g = [
    1,
    2,
    3
];
function factorial(n) {
    if (n === 0)
        return 1;
    else
        return n * factorial(n - 1);
}
factorial(a);
function print_multiple(x, y, z, a, b, c) {
    if (true) {
        func(x);
        func(y);
        return func(z);
    } else {
        func(a);
        func(b);
        return func(c);
    }
}
var z = print_multiple(1 + 2 + 3 + 4 + 5 + 6, 'a', true, [
    1,
    2,
    3,
    {
        'a': 1,
        'b': c,
        'c': [
            1,
            2,
            {
                'x': 10,
                'y': [
                    20,
                    30
                ]
            },
            3
        ]
    }
], null, {
    'a': 1,
    'b': [
        1,
        2,
        3,
        {
            'a': 1,
            'b': c,
            'c': [
                1,
                2,
                {
                    'x': 10,
                    'y': [
                        20,
                        30
                    ]
                },
                3
            ]
        }
    ]
});