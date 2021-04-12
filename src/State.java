public enum State {
    HELD, //process has locked the CS
    WANTED, // process wants access to the CS
    DO_NOT_WANT // process is currently not interested to enter the CS
}
