from companion.chat.twitch import parse_privmsg


def test_parse_privmsg_extracts_user_channel_text():
    line = ":someviewer!someviewer@someviewer.tmi.twitch.tv PRIVMSG #streamerchan :hello there!"
    result = parse_privmsg(line)
    assert result == ("someviewer", "streamerchan", "hello there!")


def test_parse_privmsg_returns_none_for_ping():
    assert parse_privmsg("PING :tmi.twitch.tv") is None


def test_parse_privmsg_returns_none_for_non_privmsg():
    line = ":tmi.twitch.tv 001 justinfan12345 :Welcome, GLHF!"
    assert parse_privmsg(line) is None


def test_parse_privmsg_handles_trailing_newline():
    line = ":user!user@user.tmi.twitch.tv PRIVMSG #chan :message text\r\n"
    result = parse_privmsg(line)
    assert result == ("user", "chan", "message text")


def test_parse_privmsg_message_with_colon_in_text():
    line = ":user!user@user.tmi.twitch.tv PRIVMSG #chan :time is 10:30 now"
    result = parse_privmsg(line)
    assert result == ("user", "chan", "time is 10:30 now")
