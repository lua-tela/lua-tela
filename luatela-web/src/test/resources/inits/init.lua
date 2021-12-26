assert(create)
assert(type(create) == 'function')

assert(create() == nil)

local status, message = pcall(create)

assert(not status)
assert(message:lower():find('.*already created.*'))

assert(time[1] == 'NANOSECONDS')
assert(time[2] == 'MICROSECONDS')
assert(time[3] == 'MILLISECONDS')
assert(time[4] == 'SECONDS')
assert(time[5] == 'MINUTES')
assert(time[6] == 'HOURS')
assert(time[7] == 'DAYS')

assert(time['NANOSECONDS'] == 'NANOSECONDS')
assert(time['MICROSECONDS'] == 'MICROSECONDS')
assert(time['MILLISECONDS'] == 'MILLISECONDS')
assert(time['SECONDS'] == 'SECONDS')
assert(time['MINUTES'] == 'MINUTES')
assert(time['HOURS'] == 'HOURS')
assert(time['DAYS'] == 'DAYS')

assert(time.NANOSECONDS == 'NANOSECONDS')
assert(time.MICROSECONDS == 'MICROSECONDS')
assert(time.MILLISECONDS == 'MILLISECONDS')
assert(time.SECONDS == 'SECONDS')
assert(time.MINUTES == 'MINUTES')
assert(time.HOURS == 'HOURS')
assert(time.DAYS == 'DAYS')

local function onetime()
    local obj = {
        my='key',
        magicnumber=525600
    }

    json.writeFile(inits .. '/onetime.json', obj)
end

local function task(pass)
    local obj, msg = json.readFile(inits .. '/tasks.json')

    if not obj then
        error('obj from JSON file should not be nil: ' .. inits .. '/tasks.json\n' .. msg)
    end

    obj.tasks = obj.tasks or 0
    obj.tasks = obj.tasks + 1
    assert(pass == obj.tasks)

    json.writeFile(inits .. '/tasks.json', obj)
end

addSingle(onetime, 1, time.SECONDS)

add(task, 1500, 'milliseconds')

finished()