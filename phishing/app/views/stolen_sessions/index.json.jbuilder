json.array!(@stolen_sessions) do |stolen_session|
  json.extract! stolen_session, :id
  json.url stolen_session_url(stolen_session, format: :json)
end
