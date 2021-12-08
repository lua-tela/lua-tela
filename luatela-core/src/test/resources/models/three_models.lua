Account = model 'account' {
    email = field 'string' {length=254},
    username = field 'string' {length=64},
    passsalt = field 'string' {length=128},
    passhash = field 'string' {length=256},
    photourl = field 'string' {length=128},
    accountstate = field 'integer' {},
    balance = field 'float' {}
}

Station = model 'station' {
    city = field 'string' {length=32},
    state = field 'string' {length=2},
    latitude = field 'float' {},
    longitude = field 'float' {},
}

StationMonthlyStats = model 'station_monthly_stats' {
    stationid = field 'integer' {primary=true},
    month = field 'integer' {primary=true},
    temp = field 'float' {},
    rain = field 'float' {},
}