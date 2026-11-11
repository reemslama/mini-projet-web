    void shouldRejectPastDeadline() {
        Instant past = now.minus(1, ChronoUnit.DAYS);
        assertThatThrownBy(() -> PublishValidator.validate(ChallengeStatus.DRAFT, 2, 3, past, now))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("date limite");
    }

    @Test
    void shouldRejectDeadlineExactlyNow() {
        assertThatThrownBy(() -> PublishValidator.validate(ChallengeStatus.DRAFT, 2, 3, now, now))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldRejectNullDeadline() {
[Output truncated. Continue reading with offset: 60]