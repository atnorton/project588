require 'test/unit'
require 'emailauth'

class EmailAuthTest < Test::Unit::TestCase
  def test_generate_token
    user_token, email_token, complete_token = EmailAuth.generateTokens
    assert_not_nil user_token
    assert_not_nil email_token
    assert_not_nil complete_token
  end

  def test_authenticate
    user_token, email_token, complete_token = EmailAuth.generateTokens
    assert EmailAuth.authenticate(user_token, email_token, complete_token)
  end

  def test_totp
    secret = ROTP::Base32.random_base32
    assert !EmailAuth.validate_totp(secret, secret)
  end

  def test_totp
    assert_not_nil EmailAuth.generateTOTPSecret
  end
end
