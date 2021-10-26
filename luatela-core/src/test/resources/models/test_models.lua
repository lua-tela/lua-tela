TestModel1 = model 'test_model1' {
    primaryField = field 'id' { primary=true },
    code = field 'string' { length=8 },
    data1 = field 'integer' { },
    data2 = field 'integer' { },
}

TestModel2 = model 'test_model2' {
    foo = field 'float' {},
    bar = field 'integer' {},
}

TestModel3 = model 'test_model3' {
    title = field 'string' { primary=true, length=64 },
    first_name = field 'string' { length=64 },
    last_name = field 'string' { length=64 },
    genre = field 'string' { length=32 },
    rating = field 'float' {},
}