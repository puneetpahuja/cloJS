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
function print_multiple(x, y, z, a, b, c) {
    if (1 === 5) {
        console.log('x :', x);
        console.log('y :', y);
        return z;
    } else {
        console.log('a :', a);
        console.log('b :', b);
        return console.log('c :', c);
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
console.log('z :', z);