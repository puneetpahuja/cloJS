const a = [
    1,
    2,
    3,
    4
];
const b = a[2];
console.log(a[3]);
if (is_array_member(form))
    get_array_member(form);
else if (is_defn(form))
    get_defn(form);
else if (is_def(form))
    get_const(form);
else if (is_if(form))
    get_if(form);
else if (is_do(form))
    get_do(form);
else if (is_vec(form))
    get_vec(form);
else if (is_lambda(form))
    get_lambda(form);
else if (is_map_ds(form))
    get_map_ds(form);
else if (is_literal(form))
    get_literal(form);
else if (is_operator(form))
    get_operator(form);
else if (is_fn_call(form))
    get_fn_call(form);
else if (true)
    null;
