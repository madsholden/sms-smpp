package com.telenordigital.sms.smpp.pdu;

/*-
 * #%L
 * sms-smpp
 * %%
 * Copyright (C) 2022 Telenor Digital
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import io.netty.buffer.ByteBufUtil;
import org.junit.jupiter.api.Test;

public class SubmitSmTest extends PduTest {
  @Test
  public void testBom() {
    final var pdu =
        SubmitSm.create(Clock.systemUTC(), "40404", "44951361920", "седмичен абонамент", null);
    final var msg = pdu.encodedShortMessage();
    final var hex = serialize(msg);
    assertThat(hex).doesNotStartWith("feff");
  }

  @Test
  public void emoji() {
    assertThat(SubmitSm.getCharset("only english")).isEqualTo(StandardCharsets.ISO_8859_1);
    assertThat(SubmitSm.getCharset("scandinavian: æøå")).isEqualTo(StandardCharsets.ISO_8859_1);
    assertThat(SubmitSm.getCharset("latin1, but not gsm: ó")).isEqualTo(StandardCharsets.UTF_16BE);

    final var charset = SubmitSm.getCharset("Hey 😬");
    assertThat(charset).isEqualTo(StandardCharsets.UTF_16BE);
    final var encoded = "Hey 😬".getBytes(charset);
    assertThat(ByteBufUtil.hexDump(encoded)).isEqualTo("0048006500790020d83dde2c");
  }

  @Test
  public void testSerialize() {
    Sequencer.sequence.set(20456);
    final var pdu = SubmitSm.create(Clock.systemUTC(), "40404", "44951361920", "¡¤#!%&/:", null);

    final var hex = serialize(pdu);

    assertThat(hex)
        .isEqualTo(
            "000000040000000000004fe80001013430343034000101343439353133363139323"
                + "00000000000000100030008a1a4232125262f3a");
  }

  @Test
  public void validityPeriod() {
    final var clock = Clock.fixed(Instant.parse("2021-04-22T13:53:40Z"), ZoneId.of("UTC"));

    assertThat(SubmitSm.validityPeriod(clock, Duration.ofMinutes(30)))
        .isEqualTo("210422142340000+")
        .hasSize(16);
  }
}
