module SessionsHelper
  def qr_code(code)
    qr_url = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=#{code}"
    image_tag(qr_url, class: "center")
  end

  def mobile_user_agent?
    request.env["HTTP_USER_AGENT"].try :match, /(android)/i
  end

  def auth_request(user, email)
    @user_token, email_token, complete_token = EmailAuth.generateTokens(32)

    # If the account is locked, don't generate these tokens
    if(is_locked?(user))
      flash[:danger] = "Account is Locked. Please unlock from Auverify App!"

      complete_token = nil
      email_token = nil
    end

    session_id = Session.generateSessionId

    session = Session.new(:user => user, :session_id => Session.encrypt(session_id), :session_key => Session.encrypt(@user_token), :auth_token => complete_token)
      session.save!

    cookies.permanent[:user_token] = { value: @user_token, httponly: true }
    cookies.permanent[:session_id] = { value: session_id, httponly: true }

    if(!is_locked?(user))
      AuthMailer.auth_email(email, email_token, request.remote_ip).deliver
    end
  end

  def is_locked?(user)
    !user.is_locked.nil? && user.is_locked > (Time.now-24.hours)
  end

  def lock_account?(user)
    user.sessions.where(created_at: (Time.now - 5.minutes)..Time.now, logged_in: nil).size >= 5
  end

  def lock(user)
    user.is_locked = Time.now
    user.save!
  end

  def unlock_user(user)
    user.sessions.where(created_at: (Time.now - 5.minutes)..Time.now, logged_in: nil).destroy_all
    user.is_locked = nil
    user.save!
  end

  def log_out
    session_id_key = Session.encrypt(cookies.permanent[:session_id])
    session = Session.find_by(session_id: session_id_key)
    session.delete
    session.save
    cookies.delete(:user_token)
    cookies.delete(:session_id)
  end

  def send_locked_email(user_token, validation_code)
    session_key = Session.encrypt(user_token)
    session = Session.where(created_at: (Time.now - 5.minutes)..Time.now).find_by(session_key: session_key)
    if session==nil || session.logged_in || session.user.nil? || !session.auth_token.nil?
      return render :json => "failed validation"
    end

    # If we fail two factor authentication, return false
    if(!validate_twofactor(session.user, validation_code))
      return render :json => "failed validation"
    end

    email_token, complete_token = EmailAuth.generateTokens_from(user_token, 32)

    session.update_attributes(:auth_token => complete_token)
    session.save!

    logger.debug "user_token:[" + user_token + "]"
    logger.debug "email_token:[" + email_token + "]"
    logger.debug "complete_token:[" + complete_token + "]"
    if(!session.authenticate(user_token, email_token)) 
      logger.debug "failure"
    else
      logger.debug "success"
    end

    AuthMailer.auth_email(session.user.email, email_token, request.remote_ip).deliver
    return true
  end

  def log_in(user_token, email_token, validation_code = nil)
    session_key = Session.encrypt(user_token)
    session = Session.where(created_at: (Time.now - 5.minutes)..Time.now).find_by(session_key: session_key)
  
    logger.debug "user_token:[" + user_token + "]"
    logger.debug "email_token:[" + email_token + "]"
    logger.debug "complete_token:[" + session.auth_token + "]"

    if session==nil || session.logged_in || session.user.nil?
      return false
    end

    @user = session.user

    # If we fail two factor authentication, return false
    if(!session.user.auth_secret.nil? && !validate_twofactor(session.user, validation_code))
      return false
    end

    begin
      session.transaction do
        if !session.logged_in && session.authenticate(user_token, email_token)
          if(@user.email.nil?)
            @user.email = @user.pending_email
            @user.confirmed = true
            @user.save!
          end

          session.logged_in = true
          session.save!
        end
      end
    rescue
      return false
    end

    # Now authenticated
    if(@user.is_locked && session.logged_in)
      unlock_user(@user)
    end

    return session.logged_in
  end

  def validate_twofactor(user, code)
    EmailAuth.validateTOTP(user.auth_secret, code)
  end

  def get_user(user_token)
    session_key = Session.encrypt(user_token)
    session = Session.find_by(session_key: session_key)
    if(!session.nil?)
      return session.user
    end
  end

  def current_user
    if cookies.permanent[:session_id]==nil
      return nil
    end
    session_id_key = Session.encrypt(cookies.permanent[:session_id])
    session = Session.find_by(session_id: session_id_key)
    if session!=nil && session.logged_in && session.user!=nil
      @current_user = session.user
    else
      @current_user = nil
    end
  end

  def signed_in?
    !current_user.nil?
  end

  def logged_in?(user)
    current_user==user
  end
end
